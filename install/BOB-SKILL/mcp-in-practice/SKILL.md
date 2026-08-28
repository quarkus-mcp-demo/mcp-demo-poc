---
name: mcp-in-practice
description: >
  Use when the user wants to provision the MCP in Practice for developers workshop,
  or when the user says their watsonx or OpenShift reservation is ready.
metadata:
  disable-model-invocation: false
---

# MCP in Practice — Workshop Provisioning

## Prerequisites

Before running any part of this skill, verify the following tools are installed.
Check each one and surface any missing tool to the user with the install instructions below.

### `oc` — OpenShift CLI ✅ required for all cluster operations

> The `openshift-mcp` npm package does **not** exist on the public registry, so
> all cluster operations use `oc` directly.

| Platform | Install |
|----------|---------|
| macOS | `brew install openshift-cli` — [Homebrew formula](https://formulae.brew.sh/formula/openshift-cli) |
| Linux | Download from [mirror.openshift.com](https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/) — extract and move `oc` to `/usr/local/bin/` |
| Windows | Download from [mirror.openshift.com](https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/openshift-client-windows.zip) — add to `PATH`; or use WSL and follow the Linux instructions |

**Verify:** `ls /usr/local/bin/oc || ls /opt/homebrew/bin/oc`

Official docs: [Installing the OpenShift CLI](https://docs.openshift.com/container-platform/latest/cli_reference/openshift_cli/getting-started-cli.html)

---

### `ansible-playbook` ✅ required for watsonx setup

| Platform | Install |
|----------|---------|
| macOS | `brew install ansible` — [Homebrew formula](https://formulae.brew.sh/formula/ansible) |
| Linux | `pip install --user ansible` — [Ansible install guide](https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html) |
| Windows | Run inside WSL: `pip install ansible` |

**Verify:** `command -v ansible-playbook || /opt/homebrew/bin/ansible-playbook --version`

---

### `jq` ✅ required for log parsing

| Platform | Install |
|----------|---------|
| macOS | `brew install jq` — [Homebrew formula](https://formulae.brew.sh/formula/jq) |
| Linux | `sudo apt-get install jq` or `sudo dnf install jq` — [jq downloads](https://jqlang.github.io/jq/download/) |
| Windows | `winget install jqlang.jq` or via WSL using the Linux instructions |

**Verify:** `command -v jq`

---

### Quick prerequisite check

```bash
echo "=== Prerequisite Check ===" && \
  (ls /usr/local/bin/oc 2>/dev/null || ls /opt/homebrew/bin/oc 2>/dev/null || command -v oc 2>/dev/null) && echo "✅ oc found" || echo "❌ oc missing — see install links above" && \
  (command -v ansible-playbook 2>/dev/null || ls /opt/homebrew/bin/ansible-playbook 2>/dev/null) && echo "✅ ansible-playbook found" || echo "❌ ansible-playbook missing — see install links above" && \
  command -v jq 2>/dev/null && echo "✅ jq found" || echo "❌ jq missing — see install links above"
```

---

## Fixed Values

| What | Value |
|------|-------|
| watsonx platform ID | `69caf0124b629d96da28b7d4` |
| OpenShift platform ID | `6a7082e7baa34c8551035ef5` |
| Playbook | `.bob/skills/mcp-in-practice/watsonx-setup.yml` |
| ArgoCD app | `mcp-demo-poc-app` (namespace: `openshift-gitops`) |
| kubeconfig path | `/tmp/openshift-debug-kubeconfig` (hardcoded in `.bob/mcp.json`) |

### Playbook defaults (overridable at provision time)

| Variable | Default | Ansible flag |
|----------|---------|--------------|
| `space_name` | `quarkus-watsonx-space` | `-e space_name=...` |
| `model_id` | `meta-llama/llama-3-3-70b-instruct` | `-e model_id=...` |
| `region` | `us-south` (only supported region) | `-e region=us-south` always passed |

The playbook is **idempotent**: if a space with `space_name` already exists it is reused, not recreated.

---

## Trigger: User wants to provision the workshop

### 1. Confirm intent

Present this confirmation:

> "I'll now set up the **MCP in Practice for Developers** workshop, which will provision:
> - A **watsonx.ai** environment (watsonx SaaS Shared to fetch an API key)
> - An **OpenShift** cluster (MCP for Developers lab)
>
> Both will be created in parallel. TechZone will email you when each is ready — watsonx typically takes ~15 minutes, OpenShift ~45 minutes. Shall I proceed?"

Use `ask_followup_question` with options **Yes, proceed** and **No, cancel**.

If the user cancels, stop.

### 2. Read the TechZone token

Read `.bob/mcp.json` and extract `mcpServers.techzone-mcp.headers["TechZone-Token"]`.

### 3. Present reservation parameters for review

```
Here are the reservation parameters I'll use. Edit anything before I proceed:

  watsonx SaaS Shared
  ├─ purpose  : Education
  ├─ start    : <now UTC — e.g. 2025-08-29T14:00:00Z>
  └─ geography: americas

  OpenShift (Quarkus lab)
  ├─ purpose  : Education
  ├─ start    : <now UTC>
  └─ geography: americas

  watsonx.ai space
  ├─ space_name : quarkus-watsonx-space
  ├─ model_id   : meta-llama/llama-3-3-70b-instruct
  └─ region     : us-south (fixed — only supported region)
```

Use `ask_followup_question` with options:
- **Looks good — create both**
- **Edit purpose** (ask: Education / Test / Demo / Event / Pilot)
- **Edit start time** (ask for date/time in their local timezone, then convert to UTC)
- **Edit geography** (ask: americas / europe / apac)
- **Edit watsonx space settings** (ask: space_name / model_id only — region is fixed)

Apply any edits and re-display until the user confirms. Store the confirmed
`space_name` and `model_id` (leave unset if not overridden). Region is always `us-south`.

### 4. Create both requests in parallel

Call these two MCP tools simultaneously:

- `techzone-mcp` → `request-mcp-techzone-create-request`
  - `platformId: 69caf0124b629d96da28b7d4`
  - `start`, `purpose`, `geography`: confirmed values
- `techzone-mcp` → `request-mcp-techzone-create-request`
  - `platformId: 6a7082e7baa34c8551035ef5`
  - `start`, `purpose`, `geography`: confirmed values

Store the returned IDs as `watsonxRequestId` and `ocpRequestId`.

### 5. Tell the user to wait for emails

Display a status card like this:

```
✅ Both reservations submitted!

  watsonx SaaS Shared  →  request ID: <watsonxRequestId>  (~15 min)
  OpenShift            →  request ID: <ocpRequestId>      (~45 min)

TechZone will email you when each environment is ready.

When you get the watsonx email, come back here and say:
  "my watsonx is ready"

When you get the OpenShift email, come back here and say:
  "my OCP is ready"

The two environments are independent — you don't need to wait
for both before acting on either one.
```

Stop here. No polling, no waiting. The user will re-enter the conversation
when they receive each TechZone email.

---

## Trigger: User says their watsonx is ready

**Detection phrases** (any of these should activate this section):
- "my watsonx is ready"
- "watsonx is ready"
- "got the watsonx email"
- "watsonx finished"
- "watsonx done"

### 1. Identify the request ID

If the user already has `watsonxRequestId` in context from earlier in the conversation,
use it. Otherwise ask: "What is your watsonx TechZone request ID?"

### 2. Fetch the request and confirm it is Ready

Call `techzone-mcp` → `request-mcp-techzone-get-request` with `watsonxRequestId`.

Check the `status` field:
- If **Ready** — proceed.
- If **Provision** — tell the user it's still provisioning and to check back in a few minutes.
- If **Failed** — display the failure reason and stop.

### 3. Extract the IBM Cloud API key

The API key is in the `output` array of the response. Search for an entry whose
`name` matches any of: `service_api_key`, `IBM Cloud Service API Key`,
`ibm_cloud_service_api_key`, `api_key`. Read its `value` field. Store as `IC_API_KEY`.

Example response structure:
```json
{
  "output": [
    { "name": "service_api_key", "label": "IBM Cloud Service API key", "value": "abc123..." }
  ]
}
```

### 4. Locate `ansible-playbook` and run the playbook

The playbook is already on disk at `.bob/skills/mcp-in-practice/watsonx-setup.yml`.

#### Detect the OS

Run via `execute_command`:

```bash
uname -s 2>/dev/null || echo "Windows"
```

- Output starts with `Darwin` → **macOS**
- Output starts with `Linux` → **Linux**
- Output is `Windows` or command fails → **Windows**

#### Resolve the path to `ansible-playbook`

**macOS / Linux** — run via `execute_command`:

```bash
command -v ansible-playbook \
  || ls /opt/homebrew/bin/ansible-playbook 2>/dev/null \
  || ls /usr/local/bin/ansible-playbook 2>/dev/null \
  || ls /usr/bin/ansible-playbook 2>/dev/null \
  || ls "$HOME/.local/bin/ansible-playbook" 2>/dev/null \
  || ls "$HOME/Library/Python/3.*/bin/ansible-playbook" 2>/dev/null \
  || find /opt /usr/local /usr "$HOME/.local" -name ansible-playbook -type f 2>/dev/null | head -1
```

Use the first path returned as `<ANSIBLE>`.

If nothing is found:
- macOS → tell the user: `ansible-playbook not found — run: brew install ansible`
- Linux → tell the user: `ansible-playbook not found — run: pip install --user ansible`
- Stop.

**Windows** — `ansible-playbook` must be run inside WSL. Run via `execute_command`:

```cmd
wsl -- bash -c "command -v ansible-playbook || find /usr /home -name ansible-playbook -type f 2>/dev/null | head -1"
```

If nothing is found, tell the user:
> `ansible-playbook not found inside WSL. Open a WSL terminal and run: pip install ansible`
Stop.

If found, the run command (below) must be prefixed with `wsl -- bash -c "..."` and the
playbook path must use the WSL translation of the Windows workspace path
(e.g. `C:\Users\...` → `/mnt/c/Users/...`).

#### Run the playbook

Build the command. Append `-e` flags **only** for values the user explicitly overrode
from the defaults. Do **not** pass `IC_API_KEY` as a `-e` flag — env var prefix only.

> **Note:** The "Wait for the space to become active" task may retry 1-2 times
> and take 2-3 minutes — this is normal. Do not interrupt.

**macOS / Linux:**

```bash
IC_API_KEY=<IC_API_KEY> <ANSIBLE> .bob/skills/mcp-in-practice/watsonx-setup.yml \
  -e region=us-south \
  [-e space_name=<space_name> if overridden] \
  [-e model_id=<model_id> if overridden] \
  2>&1 | tee /tmp/watsonx-setup.log
```

**Windows (via WSL):**

```cmd
wsl -- bash -c "IC_API_KEY=<IC_API_KEY> <ANSIBLE> /mnt/c/<workspace-path>/watsonx-setup.yml -e region=us-south [-e overrides] 2>&1 | tee /tmp/watsonx-setup.log"
```

The log is at `/tmp/watsonx-setup.log` in both cases.

### 5. Extract and display Quarkus config

The `Quarkus config` debug task in the playbook prints exactly these lines:

```
# smoke test: <PASS -> <reply> | skipped/failed (space still usable; check model_id)>
quarkus.langchain4j.watsonx.base-url=https://<region>.ml.cloud.ibm.com
quarkus.langchain4j.watsonx.space-id=<space-id>
quarkus.langchain4j.watsonx.api-key=<api-key>
quarkus.langchain4j.watsonx.chat-model.model-id=<model-id>
```

Extract them via `execute_command`:

```bash
grep -E '(# smoke test:|quarkus\.langchain4j\.watsonx\.)' /tmp/watsonx-setup.log
```

Store the extracted values:
- `WATSONX_BASE_URL` — the `base-url` value
- `WATSONX_SPACE_ID` — the `space-id` value
- `WATSONX_API_KEY` — the `api-key` value (same as `IC_API_KEY`)
- `WATSONX_MODEL_ID` — the `chat-model.model-id` value

Display the extracted lines prominently. If the smoke test says `PASS` the space is
ready for inference. If it says `skipped/failed`, note the space is still usable but
the `model_id` may need adjusting.

---

## Trigger: User says their OpenShift is ready

**Detection phrases** (any of these should activate this section):
- "my OCP is ready"
- "OCP is ready"
- "got the OpenShift email"
- "OpenShift finished"
- "OpenShift done"
- "cluster is ready"

### 1. Identify the request ID

If `ocpRequestId` is in context, use it. Otherwise ask: "What is your OpenShift TechZone request ID?"

### 2. Fetch the request and confirm it is Ready

Call `techzone-mcp` → `request-mcp-techzone-get-request` with `ocpRequestId`.

Check the `status` field:
- If **Ready** — proceed.
- If **Provision** — tell the user it's still provisioning and to check back in a few minutes.
- If **Failed** — display the failure reason and stop.

Extract the kubeconfig from the `output` array — find the entry where
`name == "conf_kubeconfig_download"` and read its `value` field (raw YAML string).
This field is confirmed to exist in the TechZone OCP request response.

### 3. Ask permission to write the kubeconfig

Before writing any credentials to disk, use `ask_followup_question` to show the
user exactly what will happen and get explicit approval:

> "I need to write the cluster kubeconfig to your local filesystem so the
> OpenShift MCP server can connect to the cluster:
>
> - **Path:** `/tmp/openshift-debug-kubeconfig`
> - **Contains:** cluster API URL + client certificate + private key
> - **Permissions:** will be set to `600` (owner read/write only)
>
> May I write this file?"

Options: **Yes, write it** / **No, cancel**

If the user cancels, stop — do not proceed with any cluster operations.

### 4. Write the kubeconfig

`write_file` is blocked outside the workspace — use `execute_command` with a
heredoc. Substitute the full kubeconfig YAML from the `conf_kubeconfig_download`
output value inline:

```bash
cat > /tmp/openshift-debug-kubeconfig << 'KUBEEOF'
<kubeconfig YAML from conf_kubeconfig_download output value>
KUBEEOF
chmod 600 /tmp/openshift-debug-kubeconfig && echo "✅ kubeconfig written"
```

Verify the file was written (size > 100 bytes) before proceeding.

> **Note:** This path is hardcoded in `.bob/mcp.json` as the `--kubeconfig`
> argument to `openshift-mcp`. The server reads it on every tool call —
> no podman, no volume mounts needed.
>
> **Important:** The openshift-mcp server uses the kubeconfig written at this
> path. If this is a new conversation, the file may not exist yet — always
> write it before making any openshift-mcp tool calls.

### 5. Check whether openshift-mcp is available

> **⚠️ Known issue:** The `openshift-mcp` npm package does **not** exist on the
> public npm registry. The MCP server configured in `.bob/mcp.json` will fail to
> start, so openshift-mcp tools will **not** be available in the session.
>
> **Always use `oc` at `/usr/local/bin/oc` instead.** It is installed on this
> machine and works directly with the kubeconfig at `/tmp/openshift-debug-kubeconfig`.

Confirm `oc` is available:
```bash
ls /usr/local/bin/oc
```

If missing, also check `/opt/homebrew/bin/oc`. If neither exists, tell the user to
install the OpenShift CLI: `brew install openshift-cli`.

> **macOS PATH note:** `node` and `npx` are installed via Homebrew but may not be
> on the shell `PATH` when running via `execute_command`. Always use full paths:
> `/opt/homebrew/bin/node`, `/opt/homebrew/bin/npx`, or prefix commands with
> `PATH="/opt/homebrew/bin:$PATH"`.

### 6. Update watsonx config on the ArgoCD Application

The watsonx configuration lives directly in `spec.source.helm.valuesObject.watsonx`
on the `mcp-demo-poc-app` ArgoCD Application itself — **not** in a separate Secret
or ConfigMap.

If the watsonx values aren't already in context (user provided them directly or they
came from the watsonx playbook output), ask:
> "Do you have the watsonx config values? I need apiKey, baseUrl, and spaceId."

The values to use:
- `apiKey` → `WATSONX_API_KEY` (the IBM Cloud service API key)
- `baseUrl` → `WATSONX_BASE_URL` (e.g. `https://us-south.ml.cloud.ibm.com`)
- `spaceId` → `WATSONX_SPACE_ID`

Patch the Application with a merge patch using `oc` — targeting only the three
fields to avoid overwriting the rest of the `valuesObject`:

```bash
KUBECONFIG=/tmp/openshift-debug-kubeconfig /usr/local/bin/oc patch application mcp-demo-poc-app \
  -n openshift-gitops --type=merge -p '{
  "spec": {
    "source": {
      "helm": {
        "valuesObject": {
          "watsonx": {
            "apiKey":   "<WATSONX_API_KEY>",
            "baseUrl":  "<WATSONX_BASE_URL>",
            "spaceId":  "<WATSONX_SPACE_ID>"
          }
        }
      }
    }
  }
}'
```

Verify the patch landed:
```bash
KUBECONFIG=/tmp/openshift-debug-kubeconfig /usr/local/bin/oc get application mcp-demo-poc-app \
  -n openshift-gitops -o jsonpath='{.spec.source.helm.valuesObject.watsonx}'
```

Confirm `apiKey`, `baseUrl`, and `spaceId` show the real values (not `replaceme`).

### 7. Trigger ArgoCD sync via oc

Use a second `oc patch --type=merge` call to set `operation.sync` on the Application,
which triggers an immediate sync. This is a **separate** patch from step 6:

```bash
KUBECONFIG=/tmp/openshift-debug-kubeconfig /usr/local/bin/oc patch application mcp-demo-poc-app \
  -n openshift-gitops --type=merge -p '{
  "operation": {
    "sync": {
      "revision": "HEAD",
      "prune": false,
      "dryRun": false
    },
    "initiatedBy": {
      "username": "bob"
    }
  }
}'
```

### 8. Poll sync status

```bash
KUBECONFIG=/tmp/openshift-debug-kubeconfig /usr/local/bin/oc get application mcp-demo-poc-app \
  -n openshift-gitops \
  -o jsonpath='Phase: {.status.operationState.phase}{"\n"}Message: {.status.operationState.message}{"\n"}Sync: {.status.sync.status}{"\n"}Health: {.status.health.status}{"\n"}'
```

Poll every 15 seconds until `Phase` is `Succeeded` or `Failed`. A phase of `Running`
with health `Degraded` is normal mid-sync — do not treat it as an error.
Report the final result to the user.

---

## Error handling

| Situation | Action |
|-----------|--------|
| Status is still `Provision` | Tell user to wait a few more minutes and come back |
| Status is `Failed` | Call `request-mcp-techzone-get-request`, display failure reason |
| 401/403 from TechZone MCP | Ask user to refresh the token in `.bob/mcp.json` |
| Playbook "Wait for space" retries | Normal — wait up to 3 minutes, do not interrupt |
| Playbook fails on another task | Show the last 20 lines of `/tmp/watsonx-setup.log` and the failed task name |
| openshift-mcp tools not available | Expected — package doesn't exist on npm. Use `oc` at `/usr/local/bin/oc` for all cluster operations |
| kubeconfig not at `/tmp/openshift-debug-kubeconfig` | Re-run step 4 (write kubeconfig heredoc) before any `oc` commands |
| `oc` commands fail with "no such file" | Check `/opt/homebrew/bin/oc` as fallback; install with `brew install openshift-cli` |
| ArgoCD sync `Running` + health `Degraded` | Normal mid-sync state — poll until `Succeeded` or `Failed` |
| ArgoCD sync `Failed` | Run the jsonpath status command from step 8 and display `status.operationState.message` |
| watsonx values already known (user provided) | Skip the watsonx trigger entirely — patch the Application directly with the provided values |
| `oc patch` on `valuesObject.watsonx` overwrites other keys | Use `--type=merge` with only the three target keys (`apiKey`, `baseUrl`, `spaceId`) — merge patch is safe |
