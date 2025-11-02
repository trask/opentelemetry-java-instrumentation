# CI Failure Analysis Plan

## Failed Jobs Summary
- Job 1: test-latest-deps / testLatestDeps1 (job ID: 54300667681)
- Job 2: test-latest-deps / testLatestDeps3 (job ID: 54300667687)
- Job 3: common / test1 (job ID: 54300667788)
- Job 4: common / test3 (job ID: 54300667793)

## Unique Failed Gradle Tasks
**Note**: Spotless tasks are excluded from this analysis as they are formatting-only checks.

- [ ] Task: :instrumentation:ratpack:ratpack-1.7:javaagent:test
  - Seen in: common / test1 (multiple parameter variants), test-latest-deps / testLatestDeps1
  - Log files: /tmp/common-test1.log, /tmp/testLatestDeps1.log
  
- [ ] Task: :instrumentation:ratpack:ratpack-1.4:javaagent:test
  - Seen in: common / test3 (multiple parameter variants), test-latest-deps / testLatestDeps3
  - Log files: /tmp/common-test3.log, /tmp/testLatestDeps3.log

## Notes
The failing jobs represent multiple matrix variants of the same Gradle tasks; job names listed here collapse duplicates that only differ by the matrix parameters noted in parentheses.
