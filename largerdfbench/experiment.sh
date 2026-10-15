#!/bin/bash

FEDERATION="LargeRDFBenchFedConf.ttl"
QUERY_DIR="queries"
STATS_DIR="stats"


# Experiment mode (change manually)
# MODE="mode1" # all enabled
MODE="mode2"   # 746 and 747 disabled
# MODE="mode3" # 746 disabled
# MODE="mode4" # 747 disabled

# Dry run (true = print commands, false = execute)
DRY_RUN=true

# Query names
QUERIES=(
    "C1.rq"
    "C3.rq"
    "C4.rq"
    "C7.rq"
    "C8.rq"
    "C10.rq"
    "L2.rq"
    "L6.rq"
    "L7.rq"
)

mkdir -p "$STATS_DIR"

echo "### MODE ${MODE} ###"
for QUERY_NAME in "${QUERIES[@]}"; do
    QUERY="${QUERY_DIR}/${QUERY_NAME}"

    # Remove .rq extension for stats filename
    QUERY_BASE="${QUERY_NAME%.rq}"
    STATS_FILE="${STATS_DIR}/${QUERY_BASE}_${MODE}.txt"

    echo "Query: ${QUERY_NAME}"

    CMD=(
        JVM_ARGS="-Xmx128g" 
        ../bin/hefquin
        --federationDescription "$FEDERATION"
        --query "$QUERY"
        --printQueryProcStatsToFile "$STATS_FILE"
    )

    if [ "$DRY_RUN" = true ]; then
        printf '   '
        printf ' %q' "${CMD[@]}"
        printf '\n'
    else
        "${CMD[@]}"
    fi
done

echo "Done."