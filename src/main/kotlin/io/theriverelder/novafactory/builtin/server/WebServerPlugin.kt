package io.theriverelder.novafactory.builtin.server

import io.ktor.http.cio.websocket.*
import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.entrance.Plugin
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.wrap
import kotlinx.coroutines.runBlocking
import java.util.Objects


fun getReactor(args: JsonObject): Reactor {
    val reactorIndex = args["reactorIndex"].number.toInt()
    return Game.factory.reactors.getOrNull(reactorIndex) ?: throw Exception("未找到索引为${reactorIndex}的反应堆")
}

fun getSlot(args: JsonObject): CellSlot {
    val reactor = getReactor(args)
    val slotNumber = args["slotNumber"].number.toInt()
    return reactor.tryGetCellSlot(slotNumber) ?: throw Exception("未找到编号为${slotNumber}的单元槽")
}

class WebServerPlugin : Plugin {
    override fun setup() {
        initializeServer()
        Game.onPostTickHandlers.add(StatePushingEventHandler())
    }

    private fun initializeServer() {
        listOf(
            CommandHandler("getFactoryInfo") { _, _, _ -> wrap { Game.factory.toInfoJson() } },
            CommandHandler("getReactorInfo") { _, args, _ -> wrap { getReactor(args).toJson() } },
            // TBD
//            CommandHandler("getFactoryHistory") { _, args, _ -> wrap { getReactor(args).toJson() } },
//            CommandHandler("getReactorHistory") { _, args, _ -> wrap { getReactor(args).toJson() } },
            CommandHandler("turnFactory") { _, args, _ -> wrap { Game.factory.turn(args["status"].boolean) } },
            CommandHandler("turnReactor") { _, args, _ -> wrap { getReactor(args).turn(args["status"].boolean) } },
            CommandHandler("setSlotDepth") { _, args, _ -> wrap { getSlot(args).depth = args["depth"].number.toDouble() } },

            CommandHandler("buy") { _, args, _ -> wrap { Game.factory.buy(args["shopItemIndex"].number.toInt()) } },
            CommandHandler("use") { _, args, _ -> wrap { Game.factory.use(
                args["reactorIndex"].number.toInt(),
                args["slotNumber"].number.toInt(),
                args["storageItemIndex"].number.toInt(),
            ) } },
            CommandHandler("pull") { _, args, _ -> wrap { Game.factory.pull(args["reactorIndex"].number.toInt(), args["slotNumber"].number.toInt()) } },
            CommandHandler("sell") { _, args, _ -> wrap { Game.factory.sell(args["storageItemIndex"].number.toInt()) } },

            CommandHandler("save") { _, args, _ -> wrap { Game.save(args["path"].string) } },
            CommandHandler("load") { _, args, _ -> wrap { Game.load(args["path"].string) } },

            CommandHandler("request") { _, args, client ->
                val requests = ArrayList<GameClientRequest>()
                for (obj in args["requestList"].array.map { it.obj }) {
                    val id = obj["id"].string
                    val req: GameClientRequest? = when (id) {
                        "reactor_info" -> ReactorInfoRequest(obj["index"].number.toInt())
                        "reactor_history" -> ReactorHistoryRequest(obj["index"].number.toInt())
                        "factory_history" -> FactoryHistoryRequest()
                        "factory_info" -> FactoryInfoRequest()
                        else -> null
                    }
                    if (req == null) return@CommandHandler ActionResult(false, "Unhandled request id $id", null)
                    else requests.add(req)
                }
                client.requests = requests.toSet()
                return@CommandHandler ActionResult(true, "", null)
            },
        ).forEach { WebServer.REG_COMMAND_HANDLER.register(it) }

        WebServer.start(8989)
    }
}

class StatePushingEventHandler : EventHandler<Game> {
    override fun handle(event: Game) {
        WebServer.clients.forEach { client ->

            val responseList = client.requests.map { it.response() }

            val pack = JsonObject(
                "type" to JsonString("state"),
                "data" to JsonArray(responseList),
            ).toJsonString()
//            println(pack)

            runBlocking { client.session.send(Frame.Text(pack)) }
        }
    }

}