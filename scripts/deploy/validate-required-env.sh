#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <value-kind> <environment-variable>..." >&2
  exit 2
fi

value_kind="$1"
shift

missing_variables=()
for variable_name in "$@"; do
  if [ -z "${!variable_name:-}" ]; then
    missing_variables+=("${variable_name}")
  fi
done

if [ "${#missing_variables[@]}" -eq 0 ]; then
  exit 0
fi

for variable_name in "${missing_variables[@]}"; do
  echo "Missing required ${value_kind}: ${variable_name}" >&2
done
exit 1
