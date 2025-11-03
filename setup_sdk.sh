#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# Define the SDK version and directories
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip"
SDK_DIR="android_sdk"
CMDLINE_TOOLS_DIR="${SDK_DIR}/cmdline-tools"
LATEST_DIR="${CMDLINE_TOOLS_DIR}/latest"

# 1. Download and unzip the command line tools
echo "Downloading Android SDK command line tools..."
wget -q --show-progress -O cmdline-tools.zip "${CMDLINE_TOOLS_URL}"

echo "Unzipping command line tools..."
mkdir -p "${LATEST_DIR}"
unzip -q cmdline-tools.zip -d "${CMDLINE_TOOLS_DIR}"
mv "${CMDLINE_TOOLS_DIR}/cmdline-tools/"* "${LATEST_DIR}"
rm -rf "${CMDLINE_TOOLS_DIR}/cmdline-tools"
rm cmdline-tools.zip

# 2. Set up environment variables for this script
export ANDROID_HOME=$(pwd)/${SDK_DIR}
export PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}

# 3. Accept licenses and install required packages
echo "Accepting SDK licenses..."
yes | sdkmanager --licenses >/dev/null

echo "Installing SDK packages..."
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

echo "Android SDK setup complete."
