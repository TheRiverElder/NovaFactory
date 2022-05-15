package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.server.WebServer

fun main() {
    initializeTestGame()
    WebServer.start(8989)
}