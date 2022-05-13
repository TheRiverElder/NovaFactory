package io.theriverelder.novafactory.httpinterfaces

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
//import io.theriverelder.novafactory.NOVA_FACTORY
import io.theriverelder.novafactory.util.io.json.convertToString
import java.lang.Integer.parseInt

//class GetReactorStatusHandler : HttpRequestHandler {
//    override val key: String = "getReactorStatus"
//
//    override suspend fun handle(context: PipelineContext<Unit, ApplicationCall>) {
//        val reactorUid: Int = try {
//            parseInt(context.context.parameters["reactorUid"])
//        } catch (e: Exception) {
//            0
//        }
//        val reactor = NOVA_FACTORY.reactors.getOrNull(reactorUid) ?: return
//        val json = reactor.toJson()
//        context.context.respondText(json.convertToString(), ContentType.Application.Json)
//    }
//}