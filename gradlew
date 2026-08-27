#!/bin/sh
# Gradle start-up script for POSIX systems (gradlew)
# See: https://docs.gradle.org/current/userguide/gradle_wrapper.html

# Resolve links: ...
APP_HOME=$(cd "$(dirname "$0")" 2>/dev/null; pwd); export APP_HOME
app_path=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$app_path" ]; then
  echo "Gradle wrapper not found. Please ensure gradle-wrapper.jar is present."
  exit 1
fi
exec java -cp "$app_path" org.gradle.wrapper.GradleWrapperMain "$@"
