package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.BuiltinPlugin
import io.theriverelder.novafactory.builtin.server.WebServer

fun main() {
    Game.use(BuiltinPlugin())
    GameLifeCycle.runGame()
    WebServer.start(8989)
}
