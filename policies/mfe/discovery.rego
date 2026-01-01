package mfe.discovery
import future.keywords.in

default allow = false

allow {
  # Allow any environment for demonstration
  environment_allowed[input.env]
  # Allow stable or canary channels
  allowed_channels[input.channel]
  not blocked_mfe
}

environment_allowed := {"production", "staging", "development"}

# Simple Set for Allowed Channels
allowed_channels := {"stable", "canary", "standard"}

deny_reason = "Channel not approved for this Environment" {
  not allow
}

blocked_mfe {
  input.mfeName in data.blocklist
}

# validate that selected version is available
version_available { 
  input.selectedVersion == input.availableVersions[_]
}

approved_domain {
  startswith(input.remoteEntry, "http://localhost:4201")
}
