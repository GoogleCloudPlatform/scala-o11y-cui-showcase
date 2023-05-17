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

# Pull in default environment variables.
source "$(dirname $0)/common_variables.sh"

# Pushes a docker image to remote artifact registry.
# Argument 1 - The local tag of a docker image.
# This function assumes:
# - GCP_REGION is  the location of the artifact registry repository region, e.g. "us-east-1"
# - GCP_PROJECT is the GCP project name holding the artifact registry, e.g "my-project"
# - GCP_REPO_NAME is the Artifact Registry docker repository name, e.g. "images"
# - The remote image name should be the same as the local tag, but with mandatory artifact registry components.
#   e.g. my-image:1.0 becomes us-east1-docker.pkg.dev/my-project/images/my-image:1.0
push_image() {
  local LOCAL_IMAGE_TAG="$1"
  local REMOTE_IMAGE_TAG="${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT}/${GCP_REPO_NAME}/${LOCAL_IMAGE_TAG}"
  echo "- " Publishing $LOCAL_IMAGE_TAG to $REMOTE_IMAGE_TAG
  docker tag "${LOCAL_IMAGE_TAG}" "${REMOTE_IMAGE_TAG}" && docker push "${REMOTE_IMAGE_TAG}"
}

# First, make sure local images are up to date.
echo "--== Building Docker Images ==--"
sbt "Docker / publishLocal"

# Note: This relies on SBT pushing project names, then docker image results on every other line.
echo "--== Publishing Docker Images ==--"
DOCKER_IMAGES=($(sbt -error 'print dockerLocalTagName' | awk 'NR % 2 == 0 { print }'))
for image in "${DOCKER_IMAGES[@]}"; do
  push_image "$image"
done