#!/usr/bin/env bash

export XTEMP=/Users/wm/git-code/learn-plugin/DlLearn/app2/src/main

echo $XTEMP

ndk-build NDK_PROJECT_PATH=$XTEMP

