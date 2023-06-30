#!/bin/bash
./gradlew clean app:assembleRelease
fir publish "./app/build/outputs/apk/release/app-release.apk"