package io.theriverelder.novafactory.httpinterfaces

import io.ktor.application.*
import io.ktor.util.pipeline.*

interface HttpRequestHandler {
    val key: String
    suspend fun handle(context: PipelineContext<Unit, ApplicationCall>)
}