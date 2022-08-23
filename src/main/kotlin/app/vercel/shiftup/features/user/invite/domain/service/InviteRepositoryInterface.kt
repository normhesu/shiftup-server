package app.vercel.shiftup.features.user.invite.domain.service

import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.invite.domain.model.Invite

interface InviteRepositoryInterface {
    suspend fun findByEmail(email: NeecEmail): Invite?
}
