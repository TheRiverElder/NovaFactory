package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.BuiltinPlugin
import io.theriverelder.novafactory.builtin.server.WebServer

fun main() {
    Game.use(BuiltinPlugin())
    WebServer.start(8989)

    while (true) {
        Thread.sleep(100)
        Game.tick()
    }
}
