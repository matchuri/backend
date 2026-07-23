#!/usr/bin/env python3

import json
import os
from pathlib import Path
import shlex


def required_environment(name: str) -> str:
    value = os.environ.get(name)
    if not value:
        raise SystemExit(f"Missing required environment variable: {name}")
    return value


remote_environment = {
    name: required_environment(name)
    for name in (
        "DEPLOY_S3_BUCKET",
        "DEPLOY_S3_PREFIX",
        "RELEASE_FILE",
        "REMOTE_ENV_FILE",
        "REMOTE_RELEASES_DIR",
        "REMOTE_DEPLOY_SCRIPT",
        "REMOTE_SERVICE_NAME",
    )
}
remote_environment["DEPLOY_S3_PREFIX"] = remote_environment[
    "DEPLOY_S3_PREFIX"
].strip("/")
remote_environment.update(
    {
        "REMOTE_HEALTH_URL": os.environ.get("REMOTE_HEALTH_URL")
        or "http://127.0.0.1:8080/api/v1/health",
        "REMOTE_HEALTH_ATTEMPTS": os.environ.get("REMOTE_HEALTH_ATTEMPTS") or "12",
        "REMOTE_HEALTH_INTERVAL_SECONDS": os.environ.get(
            "REMOTE_HEALTH_INTERVAL_SECONDS"
        )
        or "5",
        "REMOTE_RELEASE_OWNER": os.environ.get("REMOTE_RELEASE_OWNER") or "matchuri",
        "REMOTE_RELEASE_GROUP": os.environ.get("REMOTE_RELEASE_GROUP") or "matchuri",
        "REMOTE_TMP_ROOT": os.environ.get("REMOTE_TMP_ROOT")
        or "/tmp/matchuri-deploy",
    }
)

remote_script_path = Path(
    os.environ.get(
        "REMOTE_DEPLOY_PAYLOAD_SCRIPT",
        "scripts/deploy/remote-deploy.sh",
    )
)
if not remote_script_path.is_file():
    raise SystemExit(f"Remote deploy payload script not found: {remote_script_path}")

exports = "\n".join(
    f"export {name}={shlex.quote(value)}"
    for name, value in remote_environment.items()
)
remote_script = remote_script_path.read_text(encoding="utf-8")
command_script = f"{exports}\n{remote_script}"
commands = [f"bash -lc {shlex.quote(command_script)}"]

output_path = Path(os.environ.get("SSM_PARAMETERS_FILE", "ssm-parameters.json"))
output_path.write_text(
    json.dumps({"commands": commands}),
    encoding="utf-8",
)
