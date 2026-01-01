package mfe.flags

default enabled = true

# Block remote-profile if the new-ui flag is NOT true
enabled = false {
  input.mfeName == "remote-profile"
  input.features["profile.new-ui"] != true
}
