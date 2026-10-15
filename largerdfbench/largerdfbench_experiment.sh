#!/bin/bash

FEDERATION="LargeRDFBenchFedConf.ttl"
QUERY_DIR="queries"
STATS_DIR="stats"

# Experiment mode (change manually)
MODE="all_enabled"
# MODE="disabled_746_747"
# MODE="disabled_746"
# MODE="disabled_747"

# Query names
QUERIES=(
    "C1.rq"
    "C2.rq"
    "C3.rq"
    "C4.rq"
    "C5.rq"
    "C6.rq"
    "C7.rq"
    "C8.rq"
    "C9.rq"
    "C10.rq"
    "C11.rq"
    "C12.rq"
)

mkdir -p "$STATS_DIR"

for QUERY_NAME in "${QUERIES[@]}"; do
    QUERY="${QUERY_DIR}/${QUERY_NAME}"

    # Remove .rq extension for stats filename
    QUERY_BASE="${QUERY_NAME%.rq}"
    STATS_FILE="${STATS_DIR}/${QUERY_BASE}_${MODE}.txt"

    echo "Running ${QUERY_NAME} - ${MODE}"

    ./bin/hefquin \
        --federationDescription "$FEDERATION" \
        --query "$QUERY" \
        --printQueryProcStatsToFile "$STATS_FILE"
done

echo "All experiments completed for mode: $MODE"