package io.theriverelder.novafactory.httpinterfaces

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
//import io.theriverelder.novafactory.NOVA_FACTORY
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.convertToString

//class GetFactoryStatusHandler : HttpRequestHandler {
//    override val key = "getFactoryStatus"
//
//    override suspend fun handle(context: PipelineContext<Unit, ApplicationCall>) {
//        val json = JsonObject(
//            "electricityGeneratingSpeed" to JsonNumber(NOVA_FACTORY.electricityGeneratingSpeed),
//            "electricity" to JsonNumber(NOVA_FACTORY.buttery),
//            "account" to JsonNumber(NOVA_FACTORY.account),
//        )
//        context.context.respondText(json.convertToString(), ContentType.Application.Json)
//    }
//}