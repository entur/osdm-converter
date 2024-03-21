#!/bin/bash

project="ent-osdmconv-dev"
secrets=("MNG_AUTH0_INT_CLIENT_ID" "MNG_AUTH0_INT_CLIENT_SECRET" "SAMTRAFIKEN_CLIENT_SECRET" "SAMTRAFIKEN_CLIENT_ID")

for secret_name in "${secrets[@]}"; do
  value=$(gcloud secrets versions access latest --secret "$secret_name" --project "$project")
  if [ -n "$value" ]; then
    export "$secret_name"="$value"
    echo "Exported $secret_name"
  else
    echo "Failed to retrieve secret for $secret_name"
  fi
done
