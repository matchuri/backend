#!/usr/bin/env bash

set -euo pipefail

: "${SSM_COMMAND_ID:?SSM_COMMAND_ID is required}"
: "${AWS_SSM_TARGET_INSTANCE_ID:?AWS_SSM_TARGET_INSTANCE_ID is required}"

poll_attempts="${SSM_POLL_ATTEMPTS:-60}"
poll_interval_seconds="${SSM_POLL_INTERVAL_SECONDS:-5}"

if ! [[ "${poll_attempts}" =~ ^[1-9][0-9]*$ ]]; then
  echo "SSM_POLL_ATTEMPTS must be a positive integer." >&2
  exit 1
fi
if ! [[ "${poll_interval_seconds}" =~ ^[0-9]+$ ]]; then
  echo "SSM_POLL_INTERVAL_SECONDS must be a non-negative integer." >&2
  exit 1
fi

print_command_result() {
  aws ssm get-command-invocation \
    --command-id "${SSM_COMMAND_ID}" \
    --instance-id "${AWS_SSM_TARGET_INSTANCE_ID}" \
    --query "{Status:Status,StandardOutput:StandardOutputContent,StandardError:StandardErrorContent}"
}

for ((attempt = 1; attempt <= poll_attempts; attempt++)); do
  status="$(
    aws ssm get-command-invocation \
      --command-id "${SSM_COMMAND_ID}" \
      --instance-id "${AWS_SSM_TARGET_INSTANCE_ID}" \
      --query "Status" \
      --output text
  )"
  echo "Current SSM status: ${status}"

  case "${status}" in
    Success)
      print_command_result
      exit 0
      ;;
    Failed | Cancelled | TimedOut)
      print_command_result
      exit 1
      ;;
  esac

  if [ "${attempt}" -lt "${poll_attempts}" ]; then
    sleep "${poll_interval_seconds}"
  fi
done

echo "SSM command polling timed out." >&2
print_command_result || true
exit 1
