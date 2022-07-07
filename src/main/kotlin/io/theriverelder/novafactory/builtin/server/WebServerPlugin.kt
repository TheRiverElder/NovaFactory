package io.theriverelder.novafactory.builtin.server

import io.ktor.http.cio.websocket.*
import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.builtin.history.DataRecord
import io.theriverelder.novafactory.builtin.history.History
import io.theriverelder.novafactory.builtin.task.ProgramTask
import io.theriverelder.novafactory.data.getReactor
import io.theriverelder.novafactory.data.getSlot
import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.entrance.Plugin
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.persistence.parseTask
import io.theriverelder.novafactory.util.persistence.restoreTask
import io.theriverelder.novafactory.util.wrap
import kotlinx.coroutines.runBlocking
import java.util.Objects


fun getReactor(args: JsonObject): Reactor = Game.factory.getReactor(args["reactorUid"].number.toInt())

fun getSlot(args: JsonObject): CellSlot =
    Game.factory.getSlot(args["reactorUid"].number.toInt(), args["slotNumber"].number.toInt())

class WebServerPlugin : Plugin {
    override fun setup() {
        initializeServer()
        Game.onPostTickHandlers.add(StatePushingEventHandler())
    }

    private fun initializeServer() {
        listOf(
            CommandHandler("getLevelInfo") { _, _ -> wrap { Game.level?.toJson() ?: throw Exception("不存在关卡") } },
            CommandHandler("getFactoryInfo") { _, _ -> wrap { Game.factory.toInfoJson() } },
            CommandHandler("getReactorInfo") { _, args -> wrap { getReactor(args).toJson() } },

            CommandHandler("getFactoryHistory") { _, _ -> wrap { JsonArray(History.factoryHistory.getSlice().map(DataRecord::toJson)) } },
            CommandHandler("getReactorHistory") { _, args -> wrap {
                val reactorUid = args["reactorUid"].number.toInt()
                JsonArray(History.reactorHistories[reactorUid]?.getSlice()?.map(DataRecord::toJson)
                    ?: throw Exception("未找到编号为${reactorUid}的反应堆记录"))
            } },

            CommandHandler("turnFactory") { _, args -> wrap { Game.factory.turn(args["status"].boolean) } },
            CommandHandler("turnReactor") { _, args -> wrap { getReactor(args).turn(args["status"].boolean) } },
            CommandHandler("setSlotDepth") { _, args -> wrap { getSlot(args).depth = args["depth"].number.toDouble() } },

            CommandHandler("buy") { _, args -> wrap { Game.factory.buy(args["shopItemUid"].number.toInt()) } },
            CommandHandler("use") { _, args -> wrap { Game.factory.use(
                args["reactorUid"].number.toInt(),
                args["slotNumber"].number.toInt(),
                args["storageItemUid"].number.toInt(),
            ) } },

            CommandHandler("execute") { _, args -> wrap { Game.factory.addTask(ProgramTask().also { it.parse(args["commands"].string.split("\n")) }) } },

            CommandHandler("pull") { _, args -> wrap { Game.factory.pull(args["reactorUid"].number.toInt(), args["slotNumber"].number.toInt()) } },
            CommandHandler("sell") { _, args -> wrap { Game.factory.sell(args["storageItemUid"].number.toInt()) } },

            CommandHandler("save") { _, args -> wrap { Game.save(args["path"].string) } },
            CommandHandler("load") { _, args -> wrap { Game.load(args["path"].string) } },
        ).forEach { WebServer.REG_COMMAND_HANDLER.register(it) }

        WebServer.start(8989)
    }
}

class StatePushingEventHandler : EventHandler<Game> {
    override fun handle(event: Game) {
        WebServer.clients.forEach { client ->

            val pack = JsonObject(
                "type" to JsonString("state"),
                "data" to jsonNullOrUndefined(true)
            ).toJsonString()
//            println(pack)

            runBlocking { client.send(Frame.Text(pack)) }
        }
    }

}