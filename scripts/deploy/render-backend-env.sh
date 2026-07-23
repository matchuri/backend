#!/usr/bin/env bash

set -euo pipefail

: "${MATCHURI_SPRING_PROFILE:?MATCHURI_SPRING_PROFILE is required}"

output_file="${1:-backend.env}"
umask 077

{
  echo "MATCHURI_SPRING_PROFILE=${MATCHURI_SPRING_PROFILE}"
  printenv |
    grep '^MATCHURI_' |
    grep -v '^MATCHURI_SPRING_PROFILE=' |
    sort
} > "${output_file}"
