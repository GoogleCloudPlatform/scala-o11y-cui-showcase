#!/bin/bash

# Copyright 2023 Google
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


CUR_DIR=$(dirname $0)

source "${CUR_DIR}/common_variables.sh"

CRDS=($(find "$CUR_DIR/crds" -name "*.yml"))

# Function that replaces known variables in a single CRD file.
replaceConfigVariables() {
  local CRD="$1"
  cat $CRD | sed -e "s/%GCP_PROJECT%/${GCP_PROJECT}/g" -e "s/%GCP_REPO_NAME%/${GCP_REPO_NAME}/g" -e "s/%GCP_REGION%/${GCP_REGION}/g"
}

for CRD in "${CRDS[@]}"; do
  echo "Pushing $CRD"
  replaceConfigVariables "$CRD" | kubectl apply -f -
done