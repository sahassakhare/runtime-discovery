#!/bin/bash
# compile_policies.sh
# Automates the compilation of Rego policies to WASM for Embedded OPA
# Usage: ./compile_policies.sh

set -e

# Configuration
OPA_URL="https://openpolicyagent.org/downloads/v0.61.0/opa_darwin_amd64"
POLICY_DIR="../policies/mfe"
WASM_DIR="src/main/resources/policies"
OPA_BIN="./opa_tool"

# 1. Ensure OPA is available
if [ ! -f "$OPA_BIN" ]; then
    echo "[INFO] Downloading OPA binary for compilation..."
    curl -L -o "$OPA_BIN" "$OPA_URL"
    chmod +x "$OPA_BIN"
fi

echo "[OK] OPA Binary ready."

mkdir -p "$WASM_DIR"

# 2. Compile Individual Policies
# These are compiled individually so the service can load them by code (POL-MFE-XX)
# If a policy depends on others, they should be listed in the build command.

compile_policy() {
    local name=$1
    local entrypoint=$2
    shift 2
    local files=("$@")

    echo "[BUILD] Compiling $name..."
    "$OPA_BIN" build -t wasm -e "$entrypoint" "${files[@]}" -o "${WASM_DIR}/${name}.tar.gz"
    
    # Extract
    tar -xzf "${WASM_DIR}/${name}.tar.gz" -C "$WASM_DIR" /policy.wasm
    mv "${WASM_DIR}/policy.wasm" "${WASM_DIR}/${name}.wasm"
    rm "${WASM_DIR}/${name}.tar.gz"
}

# Policies
# Format: compile_policy "POLICY_CODE" "entrypoint" "source_file"
compile_policy "POL-MFE-01" "mfe/discovery/allow" "${POLICY_DIR}/discovery.rego"
compile_policy "POL-MFE-02" "mfe/access/allow" "${POLICY_DIR}/access.rego"
compile_policy "POL-MFE-03" "mfe/routing/allow" "${POLICY_DIR}/routing.rego"
compile_policy "POL-MFE-04" "mfe/compatibility/compatible" "${POLICY_DIR}/compatibility.rego"
compile_policy "POL-MFE-05" "mfe/performance/healthy" "${POLICY_DIR}/performance.rego"
compile_policy "POL-MFE-06" "mfe/flags/enabled" "${POLICY_DIR}/feature_flags.rego"
compile_policy "POL-MFE-07" "mfe/ux/compliant" "${POLICY_DIR}/ux.rego"
compile_policy "POL-MFE-08" "mfe/vetting/allow" "${POLICY_DIR}/vetting.rego"

# 3. Compile Unified Decision Policy (POL-MFE-09)
# This policy DEPENDS on all others (data.mfe.access.allow, etc.)
# So we must include ALL rego files in the build command.
echo "[BUILD] Compiling Unified Decision (POL-MFE-09)..."

# Construct list of all Rego files
ALL_REGOS=( "${POLICY_DIR}"/*.rego )

"$OPA_BIN" build -t wasm -e "mfe/decision/decision" "${ALL_REGOS[@]}" -o "${WASM_DIR}/decision.tar.gz"
tar -xzf "${WASM_DIR}/decision.tar.gz" -C "$WASM_DIR" /policy.wasm
mv "${WASM_DIR}/policy.wasm" "${WASM_DIR}/POL-MFE-09.wasm"
rm "${WASM_DIR}/decision.tar.gz"

echo "[SUCCESS] All policies compiled to WASM successfully!"
echo "[OUTPUT] Location: $WASM_DIR"
