#!/bin/bash

PKG="se.liu.ida.hefquin.vocabulary"
DIR=../src/main/java/se/liu/ida/hefquin/vocabulary/

# Check that JENA_HOME is not empty
if [ -z "$JENA_HOME" ]; then
  echo "JENA_HOME is not set"
  exit
fi

function syntaxCheck
{
    FILE="$1"
    echo "Syntax check: $FILE"
    ${JENA_HOME}/bin/riot --validate --sink "$FILE"
}

function proc
{
    TMP=TT
    FILE="$1"
    shift
    CLASS="$1"
    shift
    NS="$1"
    shift
    echo "Schemagen: $FILE"

    # -e syntax
    ${JENA_HOME}/bin/schemagen --rdfs -e TURTLE -i "$FILE" -n "$CLASS" -a "$NS" -o "$CLASS".java  "$@" 
    # Add imports
    echo "package ${PKG};" >> "$TMP"
    echo >>"$TMP"
    cat "$CLASS".java >> "$TMP"
    mv "$TMP" "$CLASS".java
    mv "$CLASS".java ${DIR}/
}

# syntaxCheck  LPGtoRDFConfiguration.ttl
# proc LPGtoRDFConfiguration.ttl  LPGtoRDF  "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#"

syntaxCheck  EngineConfiguration.ttl
proc EngineConfiguration.ttl  ECVocab  "http://www.example.org/se/liu/ida/hefquin/engineconf#"
