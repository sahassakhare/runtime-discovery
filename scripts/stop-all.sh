#!/bin/bash

echo "🛑 Stopping all MFE Discovery Platform components..."

# Ports to clear
PORTS="8181 8081 4200 4201 4203"

for PORT in $PORTS; do
  PID=$(lsof -t -i:$PORT)
  if [ -n "$PID" ]; then
    echo "Killing process on port $PORT (PID: $PID)"
    kill -9 $PID
  else
    echo "Nothing running on port $PORT"
  fi
done

echo "✅ All components stopped."
