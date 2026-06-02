#!/bin/bash

# 1. Compilar o Backend (Kotlin)
echo "Compilando Backend..."
./gradlew :boot:assemble

# 2. Mover e renomear o JAR automaticamente
echo "Movendo JAR para a pasta do Electron..."
mkdir -p electron/artifact
cp boot/build/libs/*.jar electron/artifact/boot.jar

# 3. Gerar o App Electron
echo "Gerando aplicativo descompactado..."
cd electron
npm run build:unpack

echo "Pronto! O app atualizado está em: electron/dist/mac-arm64/tp2intervals.app"