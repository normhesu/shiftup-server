package app.vercel.shiftup.features.user.invited.domain.model.value

import app.vercel.shiftup.features.user.account.domain.model.value.Role

enum class InvitedUserType(val roles: Set<Role>) {
    Cast(roles = setOf(Role.Cast)),
    Manager(roles = setOf(Role.Cast, Role.Manager))
}