#!/usr/bin/env bash

set -euo pipefail

: "${AWS_SSM_TARGET_INSTANCE_ID:?AWS_SSM_TARGET_INSTANCE_ID is required}"
: "${GITHUB_ENV:?GITHUB_ENV is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"

ssm_parameters_file="${SSM_PARAMETERS_FILE:-ssm-parameters.json}"
if [ ! -f "${ssm_parameters_file}" ]; then
  echo "SSM parameters file not found: ${ssm_parameters_file}" >&2
  exit 1
fi

command_id="$(
  aws ssm send-command \
    --instance-ids "${AWS_SSM_TARGET_INSTANCE_ID}" \
    --document-name "AWS-RunShellScript" \
    --comment "Matchuri backend deploy ${GITHUB_SHA}" \
    --parameters "file://${ssm_parameters_file}" \
    --query "Command.CommandId" \
    --output text
)"

if [ -z "${command_id}" ] || [ "${command_id}" = "None" ]; then
  echo "AWS SSM did not return a command ID." >&2
  exit 1
fi

echo "SSM_COMMAND_ID=${command_id}" >> "${GITHUB_ENV}"
