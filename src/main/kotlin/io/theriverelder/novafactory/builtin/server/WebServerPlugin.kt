package io.theriverelder.novafactory.builtin.server

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.entrance.Plugin
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.io.json.*

class WebServerPlugin : Plugin {
    override fun setup() {
        initializeServer()
        Game.onPostTickHandlers.add(StatePushingEventHandler())
    }

    fun initializeServer() {

        listOf(
            CommandHandler("buy") { _, args, _ -> Game.factory.buy(args["shopItemIndex"].number.toInt()) },
            CommandHandler("use") { _, args, _ -> Game.factory.use(
                args["itemIndex"].number.toInt(),
                args["reactorIndex"].number.toInt(),
                args["slotNumber"].number.toInt(),
            ) },
            CommandHandler("sell") { _, args, _ -> Game.factory.sell(args["shopItemIndex"].number.toInt()) },
            CommandHandler("turn") { _, args, _ ->
                val newStatus = args["status"].boolean
                val action = if (newStatus) "开启" else "暂停"
                if (newStatus == Game.running) {
                    ActionResult(false, "游戏已经${action}", Unit)
                } else {
                    Game.running = newStatus
                    if (Game.running == newStatus) ActionResult(true, "游戏${action}成功", Unit)
                    else ActionResult(false, "游戏${action}失败", Unit)
                }
            },
            CommandHandler("reactorTurn") { _, args, _ ->
                val reactorIndex = args["reactorIndex"].number.toInt()
                val reactor = Game.factory.reactors.getOrNull(reactorIndex)
                if (reactor == null) ActionResult(false, "未找到索引为${reactorIndex}的反应堆", Unit)
                else {
                    val newStatus = args["status"].boolean
                    reactor.turn(newStatus)
                }
            },
            CommandHandler("save") { _, args, _ -> Game.save(args["path"].string) },
            CommandHandler("load") { _, args, _ -> Game.load(args["path"].string) },
            CommandHandler("request") { _, args, client ->
                val requests = ArrayList<GameClientRequest>()
                for (obj in args["requestList"].array.map { it.obj }) {
                    val id = obj["id"].string
                    val req: GameClientRequest? = when (id) {
                        "reactor" -> ReactorRequest(obj["index"].number.toInt())
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
            ).toString()
//            client.session.send(pack)
            println(pack)
        }
    }

}