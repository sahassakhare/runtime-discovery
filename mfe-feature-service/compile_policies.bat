@echo off
rem compile_policies.bat
rem Automates the compilation of Rego policies to WASM on Windows
rem Usage: compile_policies.bat

set OPA_URL=https://openpolicyagent.org/downloads/v0.61.0/opa_windows_amd64.exe
set POLICY_DIR=..\policies\mfe
set WASM_DIR=src\main\resources\policies
set OPA_BIN=opa_tool.exe

rem 1. Ensure OPA is available
if not exist "%OPA_BIN%" (
    echo [INFO] Downloading OPA binary for Windows...
    curl -L -o "%OPA_BIN%" "%OPA_URL%"
)

echo [OK] OPA Binary ready.

if not exist "%WASM_DIR%" mkdir "%WASM_DIR%"

rem 2. Compile Policies
call :compile_policy POL-MFE-01 mfe/discovery/allow "%POLICY_DIR%\discovery.rego"
call :compile_policy POL-MFE-02 mfe/access/allow "%POLICY_DIR%\access.rego"
call :compile_policy POL-MFE-03 mfe/routing/allow "%POLICY_DIR%\routing.rego"
call :compile_policy POL-MFE-04 mfe/compatibility/compatible "%POLICY_DIR%\compatibility.rego"
call :compile_policy POL-MFE-05 mfe/performance/healthy "%POLICY_DIR%\performance.rego"
call :compile_policy POL-MFE-06 mfe/flags/enabled "%POLICY_DIR%\feature_flags.rego"
call :compile_policy POL-MFE-07 mfe/ux/compliant "%POLICY_DIR%\ux.rego"
call :compile_policy POL-MFE-08 mfe/vetting/allow "%POLICY_DIR%\vetting.rego"

rem 3. Compile Unified Decision Policy (POL-MFE-09)
echo [BUILD] Compiling Unified Decision (POL-MFE-09)...
rem Note: In batch, globbing is tricky, simplified to all .rego in dir if possible, 
rem but OPA build accepts directories.
"%OPA_BIN%" build -t wasm -e mfe/decision/decision "%POLICY_DIR%" -o "%WASM_DIR%\decision.tar.gz"
tar -xzf "%WASM_DIR%\decision.tar.gz" -C "%WASM_DIR%" /policy.wasm
move /Y "%WASM_DIR%\policy.wasm" "%WASM_DIR%\POL-MFE-09.wasm"
del "%WASM_DIR%\decision.tar.gz"

echo [SUCCESS] All policies compiled to WASM successfully!
exit /b 0

:compile_policy
set NAME=%1
set ENTRY=%2
set FILE=%3
echo [BUILD] Compiling %NAME%...
"%OPA_BIN%" build -t wasm -e %ENTRY% %FILE% -o "%WASM_DIR%\%NAME%.tar.gz"
tar -xzf "%WASM_DIR%\%NAME%.tar.gz" -C "%WASM_DIR%" /policy.wasm
move /Y "%WASM_DIR%\policy.wasm" "%WASM_DIR%\%NAME%.wasm"
del "%WASM_DIR%\%NAME%.tar.gz"
exit /b 0
