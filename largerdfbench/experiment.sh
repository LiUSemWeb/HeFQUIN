#!/bin/bash

FEDERATION="LargeRDFBenchFedConf.ttl"

QUERY_DIR="queries"
STATS_DIR="stats"
RESULTS_DIR="results"
SOURCE_ASSIGNMENTS_DIR="source_assignments"
LOGICAL_PLANS_DIR="logical_plans"
PHYSICAL_PLANS_DIR="physical_plans"

# Experiment modes
MODES=(
    "all-enabled"
    "all-disabled"
    #"available-ouput-hashset"
    #"join-hashset-optimizations"
    #"distinct-push-and-request-merging"
    #"project-push"
    #"project-push-and-request-merging"
    #"filter-push"
    #"filter-push-and-request-merging"
)

# DISABLE_JOIN_HASHSET_OPTIMIZATIONS
# DISABLE_AVAILABLE_OUTPUT_HASHSET
# DISABLE_DISTINCT_REQUEST_MERGING
# DISABLE_PROJECT_REQUEST_MERGING


# Dry run (true = print commands, false = execute)
DRY_RUN=false

# Number of repetitions per query
NUM_RUNS=5

# Query names
QUERIES=(
    #"C1.rq"
    "C3.rq"
    #"C4.rq"
    #"C7.rq"
    #"C8.rq"
    #"C10.rq"
    #"L2.rq"
    #"L6.rq"
    #"L7.rq"
)

# Create output directories
mkdir -p \
    "$STATS_DIR" \
    "$RESULTS_DIR" \
    "$SOURCE_ASSIGNMENTS_DIR" \
    "$LOGICAL_PLANS_DIR" \
    "$PHYSICAL_PLANS_DIR"

# add flag for PhysicalPlan

# Execute all experiment modes
for MODE in "${MODES[@]}"; do

    # Configure experiment flags
    case "$MODE" in
        all-enabled)
            DISABLE_JOIN_HASHSET_OPTIMIZATIONS=false
            DISABLE_AVAILABLE_OUTPUT_HASHSET=false
            DISABLE_DISTINCT_REQUEST_MERGING=false
            DISABLE_PROJECT_REQUEST_MERGING=false
            CONFIG="ConfDescr-all-enabled.ttl"
            ;;
        all-disabled)
            DISABLE_JOIN_HASHSET_OPTIMIZATIONS=true
            DISABLE_AVAILABLE_OUTPUT_HASHSET=true
            DISABLE_DISTINCT_REQUEST_MERGING=true
            DISABLE_PROJECT_REQUEST_MERGING=true
            CONFIG="ConfDescr-all-disabled.ttl"
            ;;
        *)
            echo "Unknown mode: $MODE" >&2
            exit 1
            ;;
    esac

    echo ""
    echo "########################################"
    echo "### MODE ${MODE}"
    echo "########################################"

    # Execute all queries
    for QUERY_NAME in "${QUERIES[@]}"; do

        QUERY="${QUERY_DIR}/${QUERY_NAME}"
        QUERY_BASE="${QUERY_NAME%.rq}"

        STATS_FILE="${STATS_DIR}/${QUERY_BASE}_${MODE}.txt"
        RESULTS_FILE="${RESULTS_DIR}/${QUERY_BASE}_${MODE}.txt"
        SOURCE_ASSIGNMENTS_FILE="${SOURCE_ASSIGNMENTS_DIR}/${QUERY_BASE}_${MODE}.txt"
        LOGICAL_PLAN_FILE="${LOGICAL_PLANS_DIR}/${QUERY_BASE}_${MODE}.txt"
        PHYSICAL_PLAN_FILE="${PHYSICAL_PLANS_DIR}/${QUERY_BASE}_${MODE}.txt"
        TIMING_FILE="timing.csv"

        echo ""
        echo "Query: ${QUERY_NAME}"

        # Repeat each query
        for ((RUN = 1; RUN <= NUM_RUNS; RUN++)); do

            echo "  Run ${RUN}/${NUM_RUNS}"

            # Create or empty output files
            for FILE in \
                "$STATS_FILE" \
                "$RESULTS_FILE" \
                "$SOURCE_ASSIGNMENTS_FILE" \
                "$LOGICAL_PLAN_FILE" \
                "$PHYSICAL_PLAN_FILE"
            do
                : > "$FILE"
            done

            # Build execution command
            CMD=(
                ../bin/hefquin
                --federationDescription "$FEDERATION"
                --confDescr "$CONFIG"
                --query "$QUERY"
                --printQueryProcStatsToFile "$STATS_FILE"
                --outputToFile "$RESULTS_FILE"
                --printSourceAssignmentToFile "$SOURCE_ASSIGNMENTS_FILE"
                --printLogicalPlanToFile "$LOGICAL_PLAN_FILE"
                --printPhysicalPlanToFile "$PHYSICAL_PLAN_FILE"
                --ignoreRetrievalCache
                --ignoreCardinalityCache
                --printQueryProcMeasurements
            )

            # Execute
            printf '%s,%s,' "$QUERY_NAME" "$MODE" >> "$TIMING_FILE"
            env \
                DISABLE_JOIN_HASHSET_OPTIMIZATIONS="$DISABLE_JOIN_HASHSET_OPTIMIZATIONS" \
                DISABLE_AVAILABLE_OUTPUT_HASHSET="$DISABLE_AVAILABLE_OUTPUT_HASHSET" \
                DISABLE_DISTINCT_REQUEST_MERGING="$DISABLE_DISTINCT_REQUEST_MERGING" \
                DISABLE_PROJECT_REQUEST_MERGING="$DISABLE_PROJECT_REQUEST_MERGING" \
                JVM_ARGS="-Xmx128g" \
                "${CMD[@]}" >> "$TIMING_FILE"
        done
    done
done

echo ""
echo "All experiments completed."
