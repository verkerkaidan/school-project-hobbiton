#!/bin/sh
set -e

if [ -z "$DOCS_USER" ] || [ -z "$DOCS_PASSWORD" ]; then
  echo "ERROR: DOCS_USER and DOCS_PASSWORD must be set" >&2
  exit 1
fi

htpasswd -bc /etc/nginx/.htpasswd "$DOCS_USER" "$DOCS_PASSWORD"

exec nginx -g "daemon off;"
