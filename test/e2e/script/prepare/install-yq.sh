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

BASE_DIR=$1
BIN_DIR=$2

if ! command -v yq &> /dev/null; then
  mkdir -p "$BASE_DIR/yq" && cd "$BASE_DIR/yq" || exit
  curl -kLo yq.tar.gz https://github.com/mikefarah/yq/archive/v4.11.1.tar.gz
  tar -zxf yq.tar.gz --strip=1
  go install && go build -ldflags -s && cp yq "$BIN_DIR/"
fi