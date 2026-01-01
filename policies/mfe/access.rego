package mfe.access

default allow = false

# Allow access to remote-profile if user is authenticated
allow {
  input.mfeName == "remote-profile"
  input.user.authenticated == true
}

# Allow remote-audit for demo purposes to see vetting policy
allow {
  input.mfeName == "remote-audit"
}

# Example: Allow admin routes only for ADMIN role (matches routing.rego intent)
allow {
  input.mfeName == "remote-profile"
  input.route == "/admin"
  input.user.roles[_] == "ADMIN"
}
deny_reason = "User must be Authenticated" {
  not allow
}
