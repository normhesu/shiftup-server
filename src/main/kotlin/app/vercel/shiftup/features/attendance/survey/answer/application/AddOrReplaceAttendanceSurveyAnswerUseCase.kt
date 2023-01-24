package app.vercel.shiftup.features.attendance.survey.answer.application

import app.vercel.shiftup.features.attendance.survey.answer.domain.service.AttendanceSurveyAnswerFactory
import app.vercel.shiftup.features.attendance.survey.answer.domain.service.AttendanceSurveyAnswerFactoryException
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
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
        availableDays: SameFiscalYearOpenCampusDates,
    ): Result<Unit, AttendanceSurveyAnswerFactoryException> = attendanceSurveyAnswerFactory(
        attendanceSurveyId = attendanceSurveyId,
        cast = getCastApplicationService(userId),
        availableDays = availableDays,
    ).onSuccess {
        attendanceSurveyAnswerRepository.addOrReplace(it)
    }.map {
        Ok(Unit)
    }
}
