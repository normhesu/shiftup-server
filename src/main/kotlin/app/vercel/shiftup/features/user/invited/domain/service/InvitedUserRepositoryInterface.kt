package app.vercel.shiftup.features.user.invited.domain.service

import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser

interface InvitedUserRepositoryInterface {
    suspend fun findByEmail(email: NeecEmail): InvitedUser?
}
