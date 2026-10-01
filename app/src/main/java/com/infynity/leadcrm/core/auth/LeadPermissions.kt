package com.infynity.leadcrm.core.auth

object LeadPermissions {

    private val allStaffRoles = setOf(
        "super_admin",
        "site_admin",
        "marketing_manager",
        "team_leader",
        "marketing_staff"
    )

    private val leadDeleteRoles = setOf(
        "super_admin",
        "site_admin",
        "marketing_manager",
        "team_leader"
    )

    fun canCreateLead(role: String): Boolean {
        return role in allStaffRoles
    }

    fun canDeleteLead(role: String): Boolean {
        return role in leadDeleteRoles
    }

    fun canDeleteTask(role: String): Boolean {
        return role in leadDeleteRoles
    }
}
