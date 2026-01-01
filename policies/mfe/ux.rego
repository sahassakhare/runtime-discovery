package mfe.ux

default compliant = true

compliant {
  input.designTokens.version >= "2.0.0"
  input.accessibility.wcag >= "2.1"
}

deny_reason := "UX governance violation" {
  not compliant
}
