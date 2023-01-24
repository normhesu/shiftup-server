package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.attendance.survey.domain.service.TallyAttendanceSurveyDomainService
import app.vercel.shiftup.features.attendance.survey.domain.service.TallyAttendanceSurveyResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val tallyAttendanceSurveyDomainService: TallyAttendanceSurveyDomainService,
) {
    suspend operator fun invoke(
        surveyId: AttendanceSurveyId,
    ): Result<TallyAttendanceSurveyResult, TallyAttendanceSurveyUseCaseException> = coroutineScope {
        val surveyDeferred = async {
            attendanceSurveyRepository.findById(surveyId)
        }
        val answersDeferred = async { attendanceSurveyAnswerRepository.findBySurveyId(surveyId) }

        tallyAttendanceSurveyDomainService(
            survey = surveyDeferred.await() ?: return@coroutineScope Err(
                TallyAttendanceSurveyUseCaseException.NotFoundSurvey,
            ),
            answers = answersDeferred.await(),
        ).let(::Ok)
    }
}

sealed class TallyAttendanceSurveyUseCaseException : Exception() {
    object NotFoundSurvey : TallyAttendanceSurveyUseCaseException()
}
