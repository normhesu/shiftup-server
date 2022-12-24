package app.vercel.shiftup.features.attendance.survey.answer.application

import app.vercel.shiftup.features.attendance.survey.answer.domain.service.AttendanceSurveyAnswerFactory
import app.vercel.shiftup.features.attendance.survey.answer.domain.service.AttendanceSurveyAnswerFactoryException
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class AddOrReplaceAttendanceSurveyAnswerUseCase(
    private val attendanceSurveyAnswerFactory: AttendanceSurveyAnswerFactory,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        userId: UserId,
        availableDays: OpenCampusDates,
    ): Result<Unit, AttendanceSurveyAnswerFactoryException.NotAvailableSurvey> {
        val answer = attendanceSurveyAnswerFactory(
            attendanceSurveyId = attendanceSurveyId,
            cast = getCastApplicationService(userId),
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
