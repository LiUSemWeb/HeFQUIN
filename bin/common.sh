#!/bin/sh

resolveLink() {
  local NAME=$1
  while [ -L "$NAME" ]; do
    case "$OSTYPE" in
      darwin*|bsd*) NAME=$(cd "$(dirname "$NAME")" && pwd -P)/$(basename "$NAME") ;;
      *) NAME=$(readlink -f "$NAME") ;;
    esac
  done
  echo "$NAME"
}

if [ -z "$HEFQUIN_HOME" ]; then
  SCRIPT=$(resolveLink "$0")
  HEFQUIN_HOME=$(cd "$(dirname "$SCRIPT")/.." && pwd)
  export HEFQUIN_HOME
fi

if [ -z "$JAVA" ]; then
  if [ -z "$JAVA_HOME" ]; then
    JAVA=$(which java)
  else
    JAVA="$JAVA_HOME/bin/java"
  fi
fi

if [ -z "$JAVA" ]; then
  echo "Cannot find a Java JDK."
  echo "Please set JAVA or JAVA_HOME and ensure java (>=Java 17) is in your PATH." 1>&2
  exit 1
fi