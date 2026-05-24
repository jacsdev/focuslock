#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

BUILD_FILE="app/build.gradle.kts"

CURRENT_VERSION=$(grep -o 'versionCode = [0-9]*' "$BUILD_FILE" | grep -o '[0-9]*')
NEW_VERSION=$((CURRENT_VERSION + 1))

sed -i '' "s/versionCode = $CURRENT_VERSION/versionCode = $NEW_VERSION/" "$BUILD_FILE"
echo "versionCode: $CURRENT_VERSION → $NEW_VERSION"

echo "Building release bundle..."
./gradlew bundleRelease

TIMESTAMP=$(date +"%Y%m%d_%H%M")
SRC="app/build/outputs/bundle/release/app-release.aab"
DEST="$HOME/Downloads/focuslock-release-v$NEW_VERSION-$TIMESTAMP.aab"

cp "$SRC" "$DEST"
echo "Bundle copied to: $DEST"
