package app.vercel.shiftup.features.user.invite.domain.service

import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.invite.domain.model.Invite

interface InviteRepositoryInterface {
    suspend fun findByEmail(email: Email): Invite?
}
