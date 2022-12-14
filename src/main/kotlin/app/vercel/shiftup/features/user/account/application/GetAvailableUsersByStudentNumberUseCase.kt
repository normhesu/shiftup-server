package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import org.koin.core.annotation.Single

@Single
class GetAvailableUsersByStudentNumberUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        studentNumbers: Iterable<StudentNumber>,
    ) = userRepository.findAvailableUserByStudentNumbers(studentNumbers)
}
