package mfe.performance

default healthy = true

healthy {
  input.metrics.errorRate < 0.1
  input.metrics.p95LoadMs < 3000
}

deny_reason := "Performance SLO violation" {
  not healthy
}
