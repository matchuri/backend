#!/usr/bin/env bash

set -euo pipefail

: "${DEPLOY_RELEASE_PREFIX:?DEPLOY_RELEASE_PREFIX is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"
: "${GITHUB_ENV:?GITHUB_ENV is required}"

mapfile -d '' executable_jars < <(
  find build/libs \
    -maxdepth 1 \
    -type f \
    -name '*.jar' \
    ! -name '*-plain.jar' \
    -print0
)

if [ "${#executable_jars[@]}" -ne 1 ]; then
  echo "Expected exactly one executable boot jar under build/libs, found ${#executable_jars[@]}." >&2
  printf 'Candidate: %s\n' "${executable_jars[@]}" >&2
  exit 1
fi

release_file="${DEPLOY_RELEASE_PREFIX}-${GITHUB_SHA}.jar"
{
  echo "RELEASE_FILE=${release_file}"
  echo "JAR_PATH=${executable_jars[0]}"
} >> "${GITHUB_ENV}"
