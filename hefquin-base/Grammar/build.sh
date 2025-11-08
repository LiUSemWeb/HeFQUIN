#!/bin/bash
## Licensed to the Apache Software Foundation (ASF) under one
## or more contributor license agreements.  See the NOTICE file
## distributed with this work for additional information
## regarding copyright ownership.  The ASF licenses this file
## to you under the Apache License, Version 2.0 (the
## "License"); you may not use this file except in compliance
## with the License.  You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.


# This file is adapted from the 'grammar' script of Apache Jena. -Olaf


# cp header.jj sparql_12_hefquin.jj
echo > sparql_12_hefquin.jj

GRAMMAR="${GRAMMAR:-main.jj}"

cat "$GRAMMAR" | cpp -P -DSPARQL -DSPARQL_12 -DSPARQL_12_HeFQUIN >> sparql_12_hefquin.jj

    FILE="sparql_12_hefquin.jj"
    PKG="sparql_12_hefquin"
    CLASS="SPARQLParser12ForHeFQUIN"

##     NAME="$(echo $N | tr '[:lower:]' '[:upper:]')"
##     DIR1="$(echo $N | tr '[:upper:]' '[:lower:]')"
    
    DIR="../src/main/java/se/liu/ida/hefquin/jenaext/sparql/lang/$PKG/javacc"

    (cd "$DIR" ; rm -f TokenMgrError.java ParseException.java Token.java JavaCharStream.java )

    echo "---- Process grammar -- $1"
    javacc -OUTPUT_DIRECTORY=$DIR  -JDK_VERSION=1.8 "${FILE}"
    RC=$?

    [ "$RC" = 0 ] || return

##     echo "---- Create HTML"
##     jjdoc -OUTPUT_FILE=${FILE%%.jj}.html "${FILE}"
    
    echo "---- Create text form"
    jjdoc -TEXT=true -OUTPUT_FILE=${FILE%%.jj}.txt "${FILE}"

    ## ---- Fixups
    
    # Fix unnecessary imports
##     echo "---- Fixing Java warnings in ${NAME}TokenManager ..."
## 
##     F="$DIR/${CLASS}TokenManager.java"
## 
##     sed -e 's/@SuppressWarnings."unused".//' \
##         -e 's/import .*//' -e 's/MatchLoop: do/do/' \
##         -e 's/int hiByte = (int)(curChar/int hiByte = (curChar/' \
## 	< $F > F
##     mv F $F

    ## JavaCharStream -- SimpleCharStream is OK.
    F="$DIR/JavaCharStream.java"
    if [ -e "$F" ]
    then
	echo "---- Fixing Javadoc warnings in JavaCharStream..."
	perl -0777 -pe 's!/\*\* (Constructor|Reinitialise).!/\* $1!sg' < $F > F
	mv F $F
    fi

##     ## TokenMgrError
##     echo "---- Fixing Java warnings in TokenMgrError"
##     F="$DIR/TokenMgrError.java"
##     if [ -e "$F" ]
##     then
## 	sed -e 's/public class TokenMgrError/\n@SuppressWarnings("all")\npublic class TokenMgrError/' < $F > F 
## 	mv F $F
##     fi

    ## -- In the parser itself
    echo "---- Fixing Java warnings in ${CLASS} ..."
    F="$DIR/${CLASS}.java"
    sed -e 's/public class /\n@SuppressWarnings("all")\npublic class /' < $F > F
    mv F $F
    echo "---- Done"
