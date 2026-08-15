name: Android CI & Release
on:
  push:
    branches: [ "main", "master" ]
    tags:
      - 'v*'
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    name: Build & Test Android APK
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle 8.9
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: 8.9

      - name: Restore debug.keystore and .env
        run: |
          if [ ! -f app/debug.keystore ]; then
            echo "Generating debug.keystore..."
            keytool -genkey -v -keystore app/debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
          fi
          if [ -f .env.example ] && [ ! -f .env ]; then
            cp .env.example .env
          fi
          if [ -f app/.env.example ] && [ ! -f app/.env ]; then
            cp app/.env.example app/.env
          fi

      - name: Build Debug APK
        run: gradle assembleDebug --no-daemon

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk

  release:
    name: Release APK on Tag
    needs: build
    if: startsWith(github.ref, 'refs/tags/')
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Setup Gradle 8.9
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: 8.9
      - name: Restore debug.keystore
        run: |
          if [ ! -f app/debug.keystore ]; then
            keytool -genkey -v -keystore app/debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
          fi
      - name: Build APK for Release
        run: gradle assembleDebug --no-daemon
      - name: Publish Release to GitHub
        uses: softprops/action-gh-release@v2
        with:
          files: app/build/outputs/apk/debug/*.apk
          generate_release_notes: true
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
