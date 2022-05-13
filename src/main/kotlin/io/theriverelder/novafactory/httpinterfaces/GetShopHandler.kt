package io.theriverelder.novafactory.httpinterfaces

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
//import io.theriverelder.novafactory.NOVA_FACTORY
import io.theriverelder.novafactory.util.io.json.JsonArray
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.convertToString

//class GetShopHandler : HttpRequestHandler {
//    override val key: String = "getShop"
//
//    override suspend fun handle(context: PipelineContext<Unit, ApplicationCall>) {
//        val json = JsonObject(
//            "items" to JsonArray(NOVA_FACTORY.shop.map { it.toJson() }),
//        )
//        context.context.respondText(json.convertToString(), ContentType.Application.Json)
//    }
//}