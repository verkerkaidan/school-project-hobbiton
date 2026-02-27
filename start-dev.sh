#!/bin/bash

# Get the directory where this script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Open 3 separate Terminal windows with the dev commands
osascript <<EOF
tell application "Terminal"
    # Window 1: Frontend
    do script "cd '$DIR' && pixi run dev-frontend"
    
    # Window 2: Connection Service
    do script "cd '$DIR' && pixi run dev-connection-service"
    
    # Window 3: Post Service
    do script "cd '$DIR' && pixi run dev-post-service"
    
    activate
end tell
EOF
