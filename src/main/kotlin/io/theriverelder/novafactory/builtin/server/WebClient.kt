package io.theriverelder.novafactory.builtin.server

import io.ktor.websocket.*

data class WebClient(
    val session: WebSocketServerSession,
) {
    var requests: Set<GameClientRequest> = HashSet()
}