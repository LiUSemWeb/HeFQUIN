#!/usr/bin/env bash

set -euo pipefail

###############################################################################
# Timing helpers
###############################################################################
now_ms() {
    perl -MTime::HiRes=time -e 'printf("%.0f\n", time()*1000)'
}

die() {
  echo "ERROR: $*" >&2
  exit 1
}

###############################################################################
# Locate repo root
###############################################################################
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
EXPECTED_SUFFIX="/hefquin-engine/src/test/bash"
REPO_ROOT="${SCRIPT_DIR%$EXPECTED_SUFFIX}"

###############################################################################
# Config (paths are resolved from repo root)
###############################################################################
HEFQUIN_BIN="${REPO_ROOT}/bin/hefquin"
FD_FILE="${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/FedbenchFedConf.ttl"
CONF_DESCR="${REPO_ROOT}/config/DefaultConfDescr.ttl"

###############################################################################
# Preconditions
###############################################################################
[[ -x "$HEFQUIN_BIN" ]] || die "HeFQUIN binary not found/executable: $HEFQUIN_BIN"
[[ -f "$FD_FILE" ]]     || die "Fedbench federation config not found: $FD_FILE"
[[ -f "$CONF_DESCR" ]]  || die "Config description not found: $CONF_DESCR"

###############################################################################
# Queries
###############################################################################
QUERIES=(
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-1.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-2.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-3.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-4.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-5.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-6.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-cd-7.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-1.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-2.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-3.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-4.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-5.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-6.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-7.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-8.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-9.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-10.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ld-11.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-1.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-2.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-3.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-4.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-5.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-6.rq"
  # "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed1-ls-7.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-1.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-2.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-3.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-4.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-5.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-6.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-cd-7.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-1.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-2.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-3.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-4.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-5.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-6.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-7.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-8.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-9.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-10.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ld-11.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-1.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-2.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-3.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-4.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-5.rq"
  "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-6.rq"
  # "${REPO_ROOT}/hefquin-engine/src/test/resources/fedbench/queries/fed2-ls-7.rq"
)

###############################################################################
# Logging
###############################################################################
LOG_DIR="${SCRIPT_DIR}/logs"
mkdir -p "$LOG_DIR"
LOG_FILE="${LOG_DIR}/fedbench_run_$(date +'%Y%m%d_%H%M%S').log"

RUN_START_MS="$(now_ms)"

echo "# Fedbench batch run started"
echo "# Writing results to: $LOG_FILE"
echo "query_file,overallQueryProcessingTime,planningTime,compilationTime,executionTime" > "$LOG_FILE"

###############################################################################
# Run queries
###############################################################################
for q in "${QUERIES[@]}"; do
  [[ -f "$q" ]] || die "Query file not found: $q"

  q_file="$(basename "$q")"
  echo "Executing ${q_file}..."
  QUERY_START_MS="$(now_ms)"
  
  # Execute query and capture output
  timings="$(
    "$HEFQUIN_BIN" \
      --query "$q" \
      --fd "$FD_FILE" \
      --confDescr "$CONF_DESCR" \
      --printQueryProcMeasurements \
      --suppressResultPrintout \
    | tr -d '[:space:]'
  )"

  echo "${q_file},${timings}" >> "$LOG_FILE"

  QUERY_END_MS="$(now_ms)"
  QUERY_TOTAL_MS=$((QUERY_END_MS - QUERY_START_MS))
  QUERY_TOTAL_S="$(perl -e "printf \"%.1f\", $QUERY_TOTAL_MS / 1000")"
  echo "Query executed in ${QUERY_TOTAL_S} seconds"
done

###############################################################################
# Summary
###############################################################################
RUN_END_MS="$(now_ms)"
RUN_TOTAL_MS=$((RUN_END_MS - RUN_START_MS))
RUN_TOTAL_S="$(perl -e "printf \"%.1f\", $RUN_TOTAL_MS / 1000")"

echo "# Fedbench batch run finished in ${RUN_TOTAL_S} seconds"
