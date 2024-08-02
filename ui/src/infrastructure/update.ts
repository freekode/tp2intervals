import {Injectable} from '@angular/core';
import {NotificationService} from "infrastructure/notification.service";
import {filter, forkJoin, map, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {EnvironmentService} from "infrastructure/environment.service";
import * as semver from "semver";


@Injectable({
  providedIn: 'root'
})
export class UpdateService {
  private static url = 'https://api.github.com';

  constructor(
    private notificationService: NotificationService,
    private environmentService: EnvironmentService,
    private configurationClient: ConfigurationClient,
    private httpClient: HttpClient
  ) {
  }

  init() {
    forkJoin([
      this.getLatestRelease(),
      this.environmentService.getVersion(),
      this.configurationClient.getConfig()
    ]).pipe(
      // filter(result => semver.gt(result[0].version, result[1])),
      // filter(result => semver.gt(result[0].version, result[2].config['generic.skip-version'] || '0.0.0')),
    ).subscribe(result => {
      this.notificationService.success(`New version ${result[0].version} is available for download on <a href="${result[0].url}" target="_blank">GitHub</a>`);
      console.log(result);
    })
  }

  private getLatestRelease(): Observable<any> {
    return this.httpClient
      .get(`${UpdateService.url}/repos/freekode/tp2intervals/releases/latest`).pipe(
        map(response => new Release(response)),
      )
  }
}

export class Release {
  version: string;
  url: string;

  constructor(json: any) {
    this.version = json['tag_name'].split('v')[1];
    this.url = json['html_url'];
  }
}
