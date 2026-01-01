package mfe.compatibility

default compatible = true

compatible {
  input.requires.angular == input.host.angular
  input.requires.rxjs == input.host.rxjs
}

deny_reason := "Angular version mismatch" {
  input.requires.angular != input.host.angular
}
