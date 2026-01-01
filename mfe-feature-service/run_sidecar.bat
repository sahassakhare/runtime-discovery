@echo off
rem run_sidecar.bat
rem Runs OPA in Server Mode (Sidecar) for local development on Windows
rem Usage: run_sidecar.bat

set OPA_URL=https://openpolicyagent.org/downloads/v0.61.0/opa_windows_amd64.exe
set POLICY_DIR=..\policies\mfe
set OPA_BIN=opa_tool.exe
set PORT=8181

rem 1. Ensure OPA is available
if not exist "%OPA_BIN%" (
    echo ⬇️  Downloading OPA binary for Windows...
    curl -L -o "%OPA_BIN%" "%OPA_URL%"
)

echo ✅ OPA Binary ready.
echo 🚀 Starting OPA Server on port %PORT%...
echo 📂 Loading policies from: %POLICY_DIR%
echo ℹ️  Update application.yml to use 'mode: sidecar'

rem Run OPA Server
rem -s: server mode
rem --addr: listening address
rem --watch: watch directory for changes (hot reloading!)
"%OPA_BIN%" run -s --addr :%PORT% --watch "%POLICY_DIR%"
