package app.vercel.shiftup.presentation.routes

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.ktor.ext.inject

inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
) = application.inject<T>(
    qualifier, parameters,
)
