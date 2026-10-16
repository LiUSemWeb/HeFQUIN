#!/bin/bash

FEDERATION="LargeRDFBenchFedConf.ttl"

QUERY_DIR="queries"
STATS_DIR="stats"
RESULTS_DIR="results"
SOURCE_ASSIGNMENTS_DIR="source_assignments"
LOGICAL_PLANS_DIR="logical_plans"
EXECUTABLE_PLANS_DIR="executable_plans"

# Experiment modes
MODES=("mode1" "mode2" "mode3" "mode4")
# mode1: all enabled
# mode2: 746 and 747 disabled
# mode3: 746 disabled
# mode4: 747 disabled

# Dry run (true = print commands, false = execute)
DRY_RUN=false

# Number of repetitions per query
NUM_RUNS=1

# Query names
QUERIES=(
    "C1.rq"
    "C3.rq"
    "C4.rq"
    "C7.rq"
    "C8.rq"
    "C10.rq"
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
    "$EXECUTABLE_PLANS_DIR"

# Execute all experiment modes
for MODE in "${MODES[@]}"; do

    # Configure experiment flags
    case "$MODE" in
        mode1)
            DISABLE_746=false
            DISABLE_747=false
            ;;
        mode2)
            DISABLE_746=true
            DISABLE_747=true
            ;;
        mode3)
            DISABLE_746=true
            DISABLE_747=false
            ;;
        mode4)
            DISABLE_746=false
            DISABLE_747=true
            ;;
        *)
            echo "Unknown mode: $MODE" >&2
            exit 1
            ;;
    esac

    echo ""
    echo "########################################"
    echo "### MODE ${MODE}"
    echo "### DISABLE_746=${DISABLE_746}"
    echo "### DISABLE_747=${DISABLE_747}"
    echo "########################################"

    # Execute all queries
    for QUERY_NAME in "${QUERIES[@]}"; do

        QUERY="${QUERY_DIR}/${QUERY_NAME}"
        QUERY_BASE="${QUERY_NAME%.rq}"

        STATS_FILE="${STATS_DIR}/${QUERY_BASE}_${MODE}.txt"
        RESULTS_FILE="${RESULTS_DIR}/${QUERY_BASE}_${MODE}.txt"
        SOURCE_ASSIGNMENTS_FILE="${SOURCE_ASSIGNMENTS_DIR}/${QUERY_BASE}_${MODE}.txt"
        LOGICAL_PLAN_FILE="${LOGICAL_PLANS_DIR}/${QUERY_BASE}_${MODE}.txt"
        EXECUTABLE_PLAN_FILE="${EXECUTABLE_PLANS_DIR}/${QUERY_BASE}_${MODE}.txt"
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
                "$EXECUTABLE_PLAN_FILE"
            do
                : > "$FILE"
            done

            # Build execution command
            CMD=(
                ../bin/hefquin
                --federationDescription "$FEDERATION"
                --query "$QUERY"
                --printQueryProcStatsToFile "$STATS_FILE"
                --outputToFile "$RESULTS_FILE"
                --printSourceAssignmentToFile "$SOURCE_ASSIGNMENTS_FILE"
                --printLogicalPlanToFile "$LOGICAL_PLAN_FILE"
                --printExecutablePlanToFile "$EXECUTABLE_PLAN_FILE"
                --ignoreRetrievalCache
                --ignoreCardinalityCache
                --printQueryProcMeasurements
            )

            # Execute or print command
            if [ "$DRY_RUN" = true ]; then
                printf '    DISABLE_746=%s DISABLE_747=%s' \
                    "$DISABLE_746" "$DISABLE_747"
                printf ' %q' "${CMD[@]}"
                printf '\n'
            else
                printf '%s,%s,' "$QUERY_NAME" "$MODE" >> "$TIMING_FILE"
                env \
                    DISABLE_746="$DISABLE_746" \
                    DISABLE_747="$DISABLE_747" \
                    JVM_ARGS="-Xmx128g" \
                    "${CMD[@]}" >> "$TIMING_FILE"
            fi

        done
    done
done

echo ""
echo "All experiments completed."
