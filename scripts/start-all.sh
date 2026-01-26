#!/bin/bash

# Configuration
BASE_DIR=$(pwd)
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

echo "🚀 Starting MFE Discovery Platform..."

# 1. Start OPA
echo "[1/5] Starting OPA Policy Engine (Port 8181)..."
docker-compose up -d opa

# 2. Start Backend Service
echo "[2/5] Starting MFE Feature Service (Port 8081)..."
cd "$BASE_DIR/mfe-feature-service"
nohup mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &

# 3. Start Dashboard UI
echo "[3/5] Starting Governing Dashboard (Port 4203)..."
cd "$BASE_DIR/dashboard-ui"
nohup npm run start -- --port 4203 > "$LOG_DIR/dashboard.log" 2>&1 &

# 4. Start Remote Profile
echo "[4/5] Starting Remote Profile MFE (Port 4201)..."
cd "$BASE_DIR/examples/remote-profile"
nohup npm run start > "$LOG_DIR/remote-profile.log" 2>&1 &

# 5. Start Host Shell
echo "[5/5] Starting Host Shell (Port 5000)..."
cd "$BASE_DIR/examples/shell"
nohup npm run start > "$LOG_DIR/shell.log" 2>&1 &

echo ""
echo "✨ Platform is booting up!"
echo "--------------------------------------------------"
echo "Shell Dashboard: http://localhost:5000"
echo "Governance UI:   http://localhost:4203"
echo "Remote MFE:      http://localhost:4201"
echo "Backend API:     http://localhost:8081"
echo "Policy Engine:   http://localhost:8181"
echo "--------------------------------------------------"
echo "Logs are available in: $LOG_DIR"
echo "Use 'tail -f logs/*.log' to monitor progress."
