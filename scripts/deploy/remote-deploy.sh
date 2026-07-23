#!/usr/bin/env bash

set -euo pipefail

: "${DEPLOY_S3_BUCKET:?DEPLOY_S3_BUCKET is required}"
: "${DEPLOY_S3_PREFIX:?DEPLOY_S3_PREFIX is required}"
: "${RELEASE_FILE:?RELEASE_FILE is required}"
: "${REMOTE_ENV_FILE:?REMOTE_ENV_FILE is required}"
: "${REMOTE_RELEASES_DIR:?REMOTE_RELEASES_DIR is required}"
: "${REMOTE_DEPLOY_SCRIPT:?REMOTE_DEPLOY_SCRIPT is required}"
: "${REMOTE_SERVICE_NAME:?REMOTE_SERVICE_NAME is required}"
: "${REMOTE_HEALTH_URL:?REMOTE_HEALTH_URL is required}"

remote_health_attempts="${REMOTE_HEALTH_ATTEMPTS:-12}"
remote_health_interval_seconds="${REMOTE_HEALTH_INTERVAL_SECONDS:-5}"
remote_release_owner="${REMOTE_RELEASE_OWNER:-matchuri}"
remote_release_group="${REMOTE_RELEASE_GROUP:-matchuri}"
remote_tmp_root="${REMOTE_TMP_ROOT:-/tmp/matchuri-deploy}"

if ! [[ "${remote_health_attempts}" =~ ^[1-9][0-9]*$ ]]; then
  echo "REMOTE_HEALTH_ATTEMPTS must be a positive integer." >&2
  exit 1
fi
if ! [[ "${remote_health_interval_seconds}" =~ ^[0-9]+$ ]]; then
  echo "REMOTE_HEALTH_INTERVAL_SECONDS must be a non-negative integer." >&2
  exit 1
fi

temporary_release="${remote_tmp_root}/${RELEASE_FILE}"
temporary_env="${remote_tmp_root}/backend.env"

cleanup() {
  rm -f "${temporary_release}" "${temporary_env}"
}
trap cleanup EXIT

mkdir -p "${remote_tmp_root}"
aws s3 cp \
  "s3://${DEPLOY_S3_BUCKET}/${DEPLOY_S3_PREFIX}/${RELEASE_FILE}" \
  "${temporary_release}"
aws s3 cp \
  "s3://${DEPLOY_S3_BUCKET}/${DEPLOY_S3_PREFIX}/backend.env" \
  "${temporary_env}"

sudo install -m 600 "${temporary_env}" "${REMOTE_ENV_FILE}"
sudo mv "${temporary_release}" "${REMOTE_RELEASES_DIR}/${RELEASE_FILE}"
sudo chown \
  "${remote_release_owner}:${remote_release_group}" \
  "${REMOTE_RELEASES_DIR}/${RELEASE_FILE}"
sudo MATCHURI_RELEASE_FILE="${RELEASE_FILE}" "${REMOTE_DEPLOY_SCRIPT}"
sudo systemctl status "${REMOTE_SERVICE_NAME}" --no-pager

for ((attempt = 1; attempt <= remote_health_attempts; attempt++)); do
  if curl --fail --silent --show-error "${REMOTE_HEALTH_URL}"; then
    exit 0
  fi

  echo "Health check attempt ${attempt}/${remote_health_attempts} failed." >&2
  if [ "${attempt}" -lt "${remote_health_attempts}" ]; then
    sleep "${remote_health_interval_seconds}"
  fi
done

echo "Matchuri health check failed after deployment." >&2
sudo journalctl -u "${REMOTE_SERVICE_NAME}" -n 100 --no-pager >&2 || true
exit 1
