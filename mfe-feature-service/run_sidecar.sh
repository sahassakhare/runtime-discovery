#!/bin/bash
# run_sidecar.sh
# Runs OPA in Server Mode (Sidecar) for local development
# Usage: ./run_sidecar.sh

set -e

# Configuration
OPA_URL="https://openpolicyagent.org/downloads/v0.61.0/opa_darwin_amd64"
POLICY_DIR="../policies/mfe"
OPA_BIN="./opa_tool"
PORT=8181

# 1. Ensure OPA is available
if [ ! -f "$OPA_BIN" ]; then
    echo "⬇️  Downloading OPA binary..."
    curl -L -o "$OPA_BIN" "$OPA_URL"
    chmod +x "$OPA_BIN"
fi

echo "✅ OPA Binary ready."
echo "🚀 Starting OPA Server on port $PORT..."
echo "📂 Loading policies from: $POLICY_DIR"
echo "ℹ️  Update application.yml to use 'mode: sidecar'"

# Run OPA Server
# -s: server mode
# --addr: listening address
# --watch: watch directory for changes (hot reloading!)
"$OPA_BIN" run -s --addr ":$PORT" --watch "$POLICY_DIR"
