#!/bin/bash

### This scripts creates the relevant Java classes for the vocabularies. To use
### this script you need to set the JENA_HOME environment variable (pointing to
### the root directory of an unzipped Jena binary package) and then uncomment
### the relevant line at the very end of this script. 

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
    PKG="$1"
    shift
    DIR="$1"
    shift
    echo "Schemagen: $FILE"

    # -e syntax
    ${JENA_HOME}/bin/schemagen --owl -e TURTLE -i "$FILE" -n "$CLASS" -a "$NS" -o "$CLASS".java  "$@" 
    # Add imports
    echo "package ${PKG};" >> "$TMP"
    echo >>"$TMP"
    cat "$CLASS".java >> "$TMP"
    mv "$TMP" "$CLASS".java
    mv "$CLASS".java ${DIR}
}

function procLPGtoRDF
{
    syntaxCheck  lpg2rdfconf.ttl
    proc lpg2rdfconf.ttl \
         LPGtoRDF \
         "http://w3id.org/hefquin/lpg2rdf#" \
         "se.liu.ida.hefquin.engine.wrappers.lpg.vocabulary" \
         ../hefquin-pgconnector/src/main/java/se/liu/ida/hefquin/engine/wrappers/lpg/vocabulary/
}

function procECVocab
{
    syntaxCheck  engineconf.ttl
    proc engineconf.ttl \
         ECVocab \
         "http://w3id.org/hefquin/engineconf#" \
         "se.liu.ida.hefquin.vocabulary" \
         ../hefquin-vocabs/src/main/java/se/liu/ida/hefquin/vocabulary/
}

function procFDVocab
{
    syntaxCheck  feddesc.ttl
    proc feddesc.ttl \
         FDVocab \
         "http://w3id.org/hefquin/feddesc#" \
         "se.liu.ida.hefquin.vocabulary" \
         ../hefquin-vocabs/src/main/java/se/liu/ida/hefquin/vocabulary/
}

### Below, uncomment the line for which you want to run the script.

procLPGtoRDF
procECVocab
procFDVocab
