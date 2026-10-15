
## Run all RSA containing DISTINCT

- with DISTINCT-related features disabled (PR #747, #746)
- with DISTINCT-related features enabled
    - disable only #747
    - disable only #746
- Record:
    - Timing
    - Query planning time
    - Cardinalities of intermediate mappings (check statistics)


## Instructions
- Disable [#746 (Use HashSet for availableOutput when mayReduce is true)](github.com/LiUSemWeb/HeFQUIN/pull/746) by:
    - setting the flag `DISABLE_MAY_REDUCE` in `PushBasedPlanThreadImplBase` to `true` 


- Disable [#747 Use hashset if mayReduce is true in sequential bind joins and optimize symmetric hash join](https://github.com/LiUSemWeb/HeFQUIN/pull/747) by:
    - setting the flag `DISABLE_MAY_REDUCE` in `BaseForExecOpParallelBindJoin` to `true`
    - setting the flag `DISABLE_MAY_REDUCE` in `BaseForExecOpSequentialBindJoin` to `true`
    - setting the flag `DISABLE_MAY_REDUCE` in `ExecOpSymmetricHashJoin` to `true`

