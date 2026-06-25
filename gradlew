#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
# Licensed under the Apache License, Version 2.0
#
# Gradle start-up script for POSIX systems (Linux, macOS)
#

# Attempt to set APP_HOME
APP_HOME="${APP_HOME:-$(cd "$(dirname "$0")" && pwd -P)}"
APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"

# Default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
