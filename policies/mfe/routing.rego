package mfe.routing
import future.keywords.in

default allow = false

allow {
  startswith(input.route, "/admin")
  "ADMIN" in input.user.roles
}

# Allow root route if authenticated
allow {
  input.route == "/"
  input.user.authenticated == true
}

# Allow canary routing ONLY if profile.routing flag is TRUE
allow {
  input.channel == "canary"
  input.features["profile.routing"] == true
}

# Allow any other route if not admin (admin is handled above)
allow {
  not startswith(input.route, "/admin")
  input.channel == "stable" # Stable is always allowed if not admin
}
