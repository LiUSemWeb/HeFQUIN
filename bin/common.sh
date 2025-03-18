#!/bin/sh
# This script resolves HEFQUIN_HOME, locates the Java binary, and sets 
# the classpath to point to the correct JAR file.

# Function to resolve symbolic links and return the absolute path of a file
resolveLink() {
  local NAME=$1  # Assign the first argument (the file name) to a local variable
  # Loop to resolve symbolic links until the actual file is found
  while [ -L "$NAME" ]; do
    case "$OSTYPE" in
      # For macOS or BSD systems, resolve the path using dirname and basename
      darwin*|bsd*) NAME=$(cd "$(dirname "$NAME")" && pwd -P)/$(basename "$NAME") ;;
      # For Linux and other systems, use readlink to resolve the full path
      *) NAME=$(readlink -f "$NAME") ;;
    esac
  done
  # Output the resolved absolute path
  echo "$NAME"
}

# If HEFQUIN_HOME is not already set, resolve it based on the script's location
if [ -z "$HEFQUIN_HOME" ]; then
  # Resolve the absolute path of the current script
  SCRIPT=$(resolveLink "$0")
  # Set HEFQUIN_HOME to the parent directory of the script's directory
  HEFQUIN_HOME=$(cd "$(dirname "$SCRIPT")/.." && pwd)
  # Export HEFQUIN_HOME so it can be used in child processes
  export HEFQUIN_HOME
fi

# If JAVA is not set, locate the Java binary
if [ -z "$JAVA" ]; then
  # If JAVA_HOME is set, use it to locate the Java binary
  if [ -z "$JAVA_HOME" ]; then
    JAVA=$(which java)  # If JAVA_HOME is not set, fall back to finding java in the system PATH
  else
    JAVA="$JAVA_HOME/bin/java"  # Use JAVA_HOME to find the Java binary
  fi
fi

# If JAVA is still not set, print an error message and exit the script
if [ -z "$JAVA" ]; then
  echo "Cannot find a Java JDK."
  echo "Please set JAVA or JAVA_HOME and ensure java (>=Java 17) is in your PATH." 1>&2
  exit 1  # Exit the script with an error code
fi

# Look for the directory that is expected to contain the hefquin-cli JAR file
if [ -d "${HEFQUIN_HOME}/libs/" ]; then
  # If the libs directory exists, use it
  HEFQUIN_JAR_DIR=${HEFQUIN_HOME}/libs/
elif [ -d "${HEFQUIN_HOME}/hefquin-cli/target/" ]; then
  # Otherwise, if hefquin-cli/target/ exists, use that one
  HEFQUIN_JAR_DIR=${HEFQUIN_HOME}/hefquin-cli/target/
else
  # Otherwise, print an error message
  echo "Cannot find the directory ${HEFQUIN_HOME}/hefquin-cli/target/"
  echo "Did you forget to compile the project?"
  exit 2  # Exit the script with an error code
fi

# After determining the directory, look for the hefquin-cli JAR file in that directory, and ..
HEFQUIN_CP=$(printf "%s\n" ${HEFQUIN_JAR_DIR}hefquin-cli-*.jar | grep -vE '(-sources|-javadoc)\.jar')
# .. check that the JAR file is actually there
if [ ! -f ${HEFQUIN_CP} ]; then
  echo "Cannot find the HeFQUIN JAR file in ${HEFQUIN_JAR_DIR}"
  exit 3  # Exit the script with an error code
fi
