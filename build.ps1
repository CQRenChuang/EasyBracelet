#!/bin/bash
./gradlew clean app:assembleRelease --stacktrace
go-fir-cli upload -f "./app/build/outputs/apk/release/app-release.apk"