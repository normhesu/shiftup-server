package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.answer.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import io.ktor.server.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        surveyId: AttendanceSurveyId,
    ): Map<OpenCampusDate, Set<Cast>> = coroutineScope {
        val surveyDeferred = async {
            attendanceSurveyRepository.findById(surveyId) ?: throw NotFoundException()
        }
        val answersDeferred = async {
            attendanceSurveyAnswerRepository.findBySurveyId(surveyId)
        }

        val openCampuses = surveyDeferred.await().tally(
            answersDeferred.await()
        )
        val availableCastUserIds = openCampuses
            .map { it.availableCastIds }
            .flatten()
            .distinct()
            .map { it.value }
        val casts: Map<CastId, Cast> = userRepository
            .findAvailableUserByIds(availableCastUserIds)
            .map(::Cast)
            .associateBy { it.id }

        openCampuses.associate { openCampus ->
            val availableCasts = openCampus.availableCastIds
                .mapNotNull { casts[it] }
                .toSet()
            openCampus.date to availableCasts
        }
    }
}
