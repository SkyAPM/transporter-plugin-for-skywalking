#!/usr/bin/env bash

# ----------------------------------------------------------------------------
# Copyright 2021 SkyAPM
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ----------------------------------------------------------------------------


set -ex

NAME=$1
CURRENT_DIR="$(cd "$(dirname "$0")"; pwd)"

# prepare base dir
TMP_DIR=/tmp/skywalking-infra-e2e
BIN_DIR=/usr/local/bin
mkdir -p $TMP_DIR && cd $TMP_DIR

# execute install
bash "$CURRENT_DIR/install-$NAME.sh" $TMP_DIR $BIN_DIR

echo "success to install $NAME"