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

function procRMLCore
{
    proc https://kg-construct.github.io/rml-resources/ontology.ttl \
         RMLVocab \
         "http://w3id.org/rml/" \
         "se.liu.ida.hefquin.rml.vocabulary" \
         ../hefquin-rml/src/main/java/se/liu/ida/hefquin/rml/vocabulary/

    echo
    echo "There is a 'Property' named 'null' in the generated Java file, which must still be renamed to 'null_'."

    echo "Another issue is that the rml:constant property is an annotation property and, thus, not reflected in the Java file. Hence, it needs to be added manually: To this end, add the following line into ../hefquin-rml/src/main/java/se/liu/ida/hefquin/rml/vocabulary/RMLVocab.java"
    echo "public static final Property constant = M_MODEL.createProperty( \"http://w3id.org/rml/constant\" );"

    echo "Yet another issue is that the rml:defaultGraph URI has not been added to the vocabulary file so far. Hence, it needs to be added manually:"
    echo "public static final Resource defaultGraph = M_MODEL.createResource( \"http://w3id.org/rml/defaultGraph\" );"
}

function procHydraVocab
{
    syntaxCheck https://www.w3.org/ns/hydra/core.jsonld
    proc https://www.w3.org/ns/hydra/core.jsonld \
         HydraVocab \
         "http://www.w3.org/ns/hydra/core#" \
         "se.liu.ida.hefquin.vocabulary" \
         ../hefquin-vocabs/src/main/java/se/liu/ida/hefquin/vocabulary/
}

### Below, uncomment the line for which you want to run the script.

#procLPGtoRDF
#procECVocab
#procFDVocab
#procRMLCore
#procHydraVocab
