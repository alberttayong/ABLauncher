#!/bin/sh
# Gradle start up script for POSIX systems
DIRNAME="$(dirname "$0")"
exec "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
  exec gradle "$@"
