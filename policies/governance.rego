package mfe.governance

default allow = false

# Rule: Allow if no violations
allow {
    not deny
}

# Rule: Deny if Environment Mismatch (Integrity Check)
deny {
    input.deployment.environment != input.target_environment
}

# Rule: Restrict Canary to Internal Users (Simulated)
# In reality, this would check JWT claims or IP ranges
deny {
    input.variant.type == "CANARY"
    input.user.is_internal == false
}

# Rule: Block Deprecated Versions (Catalog Check)
# Check if version major is too old (e.g. < 1)
deny {
    v := input.version.version
    major := split(v, ".")[0]
    to_number(major) < 1
}

# Catalog Rule: Expose reasons for denial
reason[msg] {
    input.deployment.environment != input.target_environment
    msg := sprintf("Environment mismatch: Deployment is %v but requested %v", [input.deployment.environment, input.target_environment])
}

reason[msg] {
    input.variant.type == "CANARY"
    input.user.is_internal == false
    msg := "Canary access restricted to internal users"
}
