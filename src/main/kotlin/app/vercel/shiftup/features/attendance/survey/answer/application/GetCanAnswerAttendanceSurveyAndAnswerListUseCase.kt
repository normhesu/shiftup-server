package app.vercel.shiftup.features.attendance.survey.answer.application

import app.vercel.shiftup.features.attendance.survey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class GetCanAnswerAttendanceSurveyAndAnswerListUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
    ): List<Pair<AttendanceSurvey, AttendanceSurveyAnswer?>> = coroutineScope {
        val castDeferred = async { getCastApplicationService(userId) }
        val answersDeferred = async {
            attendanceSurveyAnswerRepository.findByCastId(CastId.unsafe(userId))
        }
        val surveysDeferred = async { attendanceSurveyRepository.findAll() }

        val cast = castDeferred.await()
        val answers = answersDeferred.await()

        // アンケートが大量に保存されることはないため、取得後にfilterをする
        surveysDeferred.await()
            .filter { survey ->
                survey.canAnswer(cast)
            }.map { survey ->
                survey to answers.find { it.surveyId == survey.id }
            }
    }
}
