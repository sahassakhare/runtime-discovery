package mfe.vetting

default allow = false

# Access to highly sensitive MFEs requires deep context verification
allow {
    # 1. Target check
    input.mfeName == "remote-audit"

    # 2. Department check (Finance or Security only)
    allowed_departments[input.user.department]

    # 3. Role check
    input.user.roles[_] == "SECURITY"

    # 4. Citizenship check (Internal only)
    input.user.isInternal == true
}

# Allow all other MFEs to bypass vetting (handled by other policies)
allow {
    input.mfeName != "remote-audit"
}

allowed_departments := {"FINANCE", "SECURITY", "LEGAL"}

deny_reason = "Vetting Failed: Highly sensitive MFE requires SECURITY role and Finance/Security department" {
    not allow
}
