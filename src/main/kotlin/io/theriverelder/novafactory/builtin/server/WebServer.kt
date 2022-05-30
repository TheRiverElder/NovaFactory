package io.theriverelder.novafactory.builtin.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.registry.Registry

object WebServer {

    val REG_COMMAND_HANDLER = Registry<String, CommandHandler> { it.head }

    val clients = HashSet<WebClient>()
    lateinit var originalServer: ApplicationEngine

    fun start(port: Int) {
        originalServer = embeddedServer(Netty, port) {
            configureRouting()
        }.start(wait = false)
    }

    private fun Application.configureRouting() {
        install(WebSockets)

        // Starting point for a Ktor app:
        routing {
            get("/") {
                call.respondText("Please use WebSocket instead.")
            }
            webSocket("/") {
                println("client in: ${this.hashCode()}")
                val client = WebClient(this)
                clients.add(client)
                for (frame in incoming) {
                    val str = frame.data.decodeToString()
                    println(str)
                    val json = deserialize(StringReader(str))
                    if (json != null) {
                        handleCommand(json.obj, client)
                    }
                }
                clients.remove(client)
                println("client out: ${this.hashCode()}")
            }
        }
    }

    suspend fun broadcast(json: JsonSerializable) {
        if (clients.isNotEmpty()) {
            val strData = json.convertToString()
            broadcast(strData)
        }
    }

    suspend fun broadcast(json: ToJson) {
        if (clients.isNotEmpty()) {
            broadcast(json.toJson())
        }
    }

    suspend fun broadcast(string: String) {
        clients.forEach { it.session.send(string) }
    }

    suspend fun handleCommand(json: JsonObject, client: WebClient) {
        val head = json["head"].string
        val args = json["args"].obj

        val handler = REG_COMMAND_HANDLER.tryGet(head) ?: throw Exception("未能处理命令：$head")

        val res: ActionResult<String, *> = handler.handle(head, args, client)
        val response = JsonObject(
            "type" to JsonString("response"),
            "succeeded" to jsonBoolean(res.succeeded),
            "message" to JsonString(res.message),
        )
        broadcast(response)
    }

}