package mfe.decision

default decision = {
  "allow": false,
  "reason": "Policy denied"
}

decision = {
  "allow": true,
  "selectedVersion": input.selectedVersion,
  "remoteEntry": input.remoteEntry,
  "exposedModule": input.exposedModule,
  "fallback": input.fallback
} {
  data.mfe.discovery.allow
  data.mfe.access.allow
  data.mfe.routing.allow
  data.mfe.compatibility.compatible
  data.mfe.performance.healthy
  data.mfe.flags.enabled
  data.mfe.vetting.allow
}
