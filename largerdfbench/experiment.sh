#!/bin/bash

#############
### FLAGS ###
#############

# DISABLE_JOIN_HASHSET_OPTIMIZATIONS:
#   BaseForExecOpParallelBindJoin.java
#     - Prevents duplicate solMaps from being skipped within the current batch. (#747)
#     - Disables the use of HashSet for solMapsCoveredByCurrentBatch. (#747)
#   BaseForExecOpSequentialBindJoin.java
#     - Prevents duplicate solMaps from being skipped within the current batch. (#747)
#     - Disables the use of HashSet for currentBatch. (#747)
#   ExecOpSymmetricHashJoin.java
#     - Disables checking the accumulated input index for an already-seen mapping before inserting and probing. (#747)
# DISABLE_AVAILABLE_OUTPUT_HASHSET:
#   PushBasedPlanThreadImplBase.java
#     - Disables the use of a HashSet to buffer output solution mappings to remove duplicates.
# DISABLE_PROJECT_REQUEST_MERGING:
#   MergeRequests.java
#     - Disables merging project operator into the request.
# DISABLE_FILTER_REQUEST_MERGING:
#   MergeRequests.java
#     - Disables merging filter operator into the request.

#######
# BUG #
#######
# Line: 766–773 in ProjectPushDown seems not to work correctly. Commented out. Example plans for L7 in bug/.
# // If the top project is redundant, drop it
# if ( projectOp.getVariables().equals(allPushdownVars) ) {
#     return newJoin;
# }


QUERY_DIR="queries"
STATS_DIR="stats"
RESULTS_DIR="results"
SOURCE_ASSIGNMENTS_DIR="source_assignments"
LOGICAL_PLANS_DIR="logical_plans"
PHYSICAL_PLANS_DIR="physical_plans"

# Create output directories
mkdir -p \
    "$STATS_DIR" \
    "$RESULTS_DIR" \
    "$SOURCE_ASSIGNMENTS_DIR" \
    "$LOGICAL_PLANS_DIR" \
    "$PHYSICAL_PLANS_DIR"

FEDERATION="LargeRDFBenchFedConf.ttl"

# Experiment modes
MODES=(
    "DISABLE_ALL"
    "ENABLE_ALL"
    "ENABLE_JOIN_HASHSET_OPTIMIZATIONS"
    "ENABLE_AVAILABLE_OUTPUT_HASHSET"
    "ENABLE_PROJECT_REQUEST_MERGING"
    "ENABLE_FILTER_REQUEST_MERGING"
)

# Number of repetitions per query
NUM_RUNS=10

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

# Execute all queries
for QUERY_NAME in "${QUERIES[@]}"; do
    # Repeat each query
    for ((RUN = 1; RUN <= NUM_RUNS; RUN++)); do
        # Execute all experiment modes
        for MODE in "${MODES[@]}"; do

            # Disable all be default
            DISABLE_JOIN_HASHSET_OPTIMIZATIONS=true
            DISABLE_AVAILABLE_OUTPUT_HASHSET=true
            DISABLE_PROJECT_REQUEST_MERGING=true
            DISABLE_FILTER_REQUEST_MERGING=true

            CONFIG="ConfDescr-all-enabled.ttl"

            case "$MODE" in
                DISABLE_ALL)
                    # Disables heuristics: FilterPushDown, ProjectPushDown and ProjectRemoval
                    CONFIG="ConfDescr-all-disabled.ttl"
                    ;;
                ENABLE_ALL)
                    DISABLE_JOIN_HASHSET_OPTIMIZATIONS=false
                    DISABLE_AVAILABLE_OUTPUT_HASHSET=false
                    DISABLE_PROJECT_REQUEST_MERGING=false
                    DISABLE_FILTER_REQUEST_MERGING=false
                    ;;
                ENABLE_JOIN_HASHSET_OPTIMIZATIONS)
                    DISABLE_JOIN_HASHSET_OPTIMIZATIONS=false
                    ;;
                ENABLE_AVAILABLE_OUTPUT_HASHSET)
                    DISABLE_AVAILABLE_OUTPUT_HASHSET=false
                    ;;
                ENABLE_PROJECT_REQUEST_MERGING)
                    DISABLE_PROJECT_REQUEST_MERGING=false
                    ;;
                ENABLE_FILTER_REQUEST_MERGING)
                    DISABLE_FILTER_REQUEST_MERGING=false
                    ;;
                *)
                    echo "Unknown mode: $MODE" >&2
                    exit 1
                    ;;
            esac

            # Dirs
            QUERY="${QUERY_DIR}/${QUERY_NAME}"
            QUERY_BASE="${QUERY_NAME%.rq}"
            STATS_FILE="${STATS_DIR}/${QUERY_BASE}_${MODE}.txt"
            RESULTS_FILE="${RESULTS_DIR}/${QUERY_BASE}_${MODE}.txt"
            SOURCE_ASSIGNMENTS_FILE="${SOURCE_ASSIGNMENTS_DIR}/${QUERY_BASE}_${MODE}.txt"
            LOGICAL_PLAN_FILE="${LOGICAL_PLANS_DIR}/${QUERY_BASE}_${MODE}.txt"
            PHYSICAL_PLAN_FILE="${PHYSICAL_PLANS_DIR}/${QUERY_BASE}_${MODE}.txt"
            TIMING_FILE="timing.csv"

            echo "Mode: ${MODE}"
            echo "  Query: ${QUERY_NAME}"
            echo "    Run: ${RUN}/${NUM_RUNS}"

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
                --printQueryProcMeasurements
            )

            # Execute
            printf '%s, %s, ' "$QUERY_NAME" "$MODE" >> "$TIMING_FILE"
            env \
                DISABLE_MAY_REDUCE="$DISABLE_MAY_REDUCE" \
                DISABLE_JOIN_HASHSET_OPTIMIZATIONS="$DISABLE_JOIN_HASHSET_OPTIMIZATIONS" \
                DISABLE_AVAILABLE_OUTPUT_HASHSET="$DISABLE_AVAILABLE_OUTPUT_HASHSET" \
                DISABLE_PROJECT_REQUEST_MERGING="$DISABLE_PROJECT_REQUEST_MERGING" \
                DISABLE_FILTER_REQUEST_MERGING="$DISABLE_FILTER_REQUEST_MERGING" \
                JVM_ARGS="-Xmx128g" \
                "${CMD[@]}" >> "$TIMING_FILE"
        done
    done
done

echo ""
echo "All experiments completed."
