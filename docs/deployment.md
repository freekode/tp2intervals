# Optional private deployment

The root [upstream Compose file](../docker-compose.yml) remains the default
for `docker compose up`. It keeps the existing upstream image, SQLite file
mount, and port binding. Use this optional deployment to opt in to a loopback
binding, a dedicated SQLite directory, or a privately built image.

## Use a verified image

Use Docker Engine and Docker Compose v2. Load a verified image containing the
features you plan to use. The fork's synthetic and native release testing has
used `linux/amd64`. Validate a matching build before using another platform.

```sh
umask 077
cp .env.example .env
mkdir -p data
```

Set `TP2INTERVALS_IMAGE` in `.env` to the verified local image reference.
The private example never pulls it. Set a high-entropy
`TP2INTERVALS_PREFLIGHT_TOKEN` privately to enable review operations. Leave
scheduled forward synchronization disabled until after baseline review and
activation. The optional example starts in preview-only mode.

```sh
docker compose -f docker-compose.private.yaml config --quiet
docker compose -f docker-compose.private.yaml up -d
```

Open `http://127.0.0.1:8080`, or set `TP2INTERVALS_HOST_PORT` for a different
loopback port. For existing installations, stop and drain writers, take a
consistent SQLite backup including committed WAL data, and seed
`./data/tp2intervals.sqlite` from that backup **before** switching to this
directory mount. Keep the original database and preserve the ledger. Users
of the earlier private example should select `docker-compose.private.yaml`
explicitly and set `TP2INTERVALS_IMAGE` to their existing verified image.

For an existing proxy, set `TP2INTERVALS_PROXY_NETWORK` and the exact Tomcat
trusted-peer IP regex. Compose 2.24.4 or newer supports the overlay's port
reset. The overlay supplies no routing rules. Keep the app private and verify
forwarded-header sanitization at ingress.

```sh
docker compose -f docker-compose.private.yaml -f docker-compose.proxy.yaml config --quiet
docker compose -f docker-compose.private.yaml -f docker-compose.proxy.yaml up -d
```

[Proxy and operational runbook](trainingpeaks-sync-ledger.md)

## Build an optional fork or local image

Use JDK 21 and Node.js. In a clean build directory, run `npm ci --prefix ui`
and `npm run build --prefix ui`. Copy the contents of `ui/dist/ui/browser`
into `boot/src/main/resources/static`, then run `(cd boot && ./gradlew bootJar)`.
The repository Dockerfile packages the resulting JAR. Choose a local image tag
and pass it to `docker build -t IMAGE .`. Use that tag as
`TP2INTERVALS_IMAGE` in the optional private Compose file.

The [legacy preview cutover overlay](../docker-compose.preview.example.yml)
can still be layered onto an existing installation with its
`TP2INTERVALS_FORK_IMAGE` and `TP2INTERVALS_DATA_DIR` inputs.
