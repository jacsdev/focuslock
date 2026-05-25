#!/bin/bash
set -e
cd "$(dirname "$0")"
bundle exec fastlane local
