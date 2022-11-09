package app.vercel.shiftup.features.attendancesurvey.application

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyAnswerFactory
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyAnswerFactoryException
import app.vercel.shiftup.features.attendancesurvey.infra.AttendanceSurveyRepository
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.ktor.server.plugins.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
class AddOrReplaceAttendanceSurveyAnswerUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerFactory: AttendanceSurveyAnswerFactory,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        userId: UserId,
        availableDays: OpenCampusDates,
    ): Result<Unit, AttendanceSurveyAnswerFactoryException.NotAvailableSurvey> = mutex.withLock {
        val survey = attendanceSurveyRepository.findById(attendanceSurveyId)
            .let(::requireNotNull)
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
        attendanceSurveyRepository.replace(
            survey.addOrReplaceAnswer(answer)
        )
        return Ok(Unit)
    }
}

private val mutex = Mutex()
