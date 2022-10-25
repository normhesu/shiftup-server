package app.vercel.shiftup.features.attendancesurvey.answer.application

import app.vercel.shiftup.features.attendancesurvey.answer.domain.service.AttendanceSurveyAnswerFactory
import app.vercel.shiftup.features.attendancesurvey.answer.domain.service.AttendanceSurveyAnswerFactoryException
import app.vercel.shiftup.features.attendancesurvey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class AddOrUpdateAttendanceSurveyAnswerUseCase(
    private val attendanceSurveyAnswerFactory: AttendanceSurveyAnswerFactory,
    private val userRepository: UserRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        userId: UserId,
        availableDays: OpenCampusDates,
    ): Result<Unit, AttendanceSurveyAnswerFactoryException.NotAvailableSurvey> {
        val cast = userRepository.findById(userId)
            .let(::requireNotNull)
            .let(::Cast)
        val answer = attendanceSurveyAnswerFactory(
            attendanceSurveyId = attendanceSurveyId,
            cast = cast,
            availableDays = availableDays,
        ).getOrElse {
            when (it) {
                is AttendanceSurveyAnswerFactoryException.NotFoundSurvey -> throw NotFoundException()
                is AttendanceSurveyAnswerFactoryException.NotAvailableSurvey -> return Err(it)
            }
        }
        attendanceSurveyAnswerRepository.addOrReplace(answer)
        return Ok(Unit)
    }
}
