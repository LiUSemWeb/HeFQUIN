#!/bin/bash

# Ensure RELEASE_VERSION is set
if [ -z "$RELEASE_VERSION" ]; then
  echo "Error: RELEASE_VERSION is not set."
  echo "Usage: ./prepare_dist.sh [--dry-run]"
  exit 1
fi

DRY_RUN=false

# Check for --dry-run argument
if [ "$1" == "--dry-run" ]; then
  DRY_RUN=true
  echo "Running in DRY RUN mode. No files will be copied, deleted, or zipped."
fi

run() {
  if $DRY_RUN; then
    echo "[DRY RUN] $*"
  else
    eval "$@"
  fi
}

run mkdir -p ./HeFQUIN-${RELEASE_VERSION}/libs
run cp ./hefquin-cli/target/hefquin-cli-${RELEASE_VERSION}.jar ./HeFQUIN-${RELEASE_VERSION}/libs/
run cp -r ./bin ./HeFQUIN-${RELEASE_VERSION}/
run cp -r ./bat ./HeFQUIN-${RELEASE_VERSION}/
run cp ./release-assets/README.txt ./HeFQUIN-${RELEASE_VERSION}/
run zip -r HeFQUIN-${RELEASE_VERSION}.zip ./HeFQUIN-${RELEASE_VERSION}
run rm -rf ./HeFQUIN-${RELEASE_VERSION}