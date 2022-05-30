package io.theriverelder.novafactory

import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask

object GameLifeCycle {

    public var period: Long = 500

    public var running: Boolean = false
        private set

    private val timer: Timer = Timer()
    var gameLoop: GameLoop = GameLoop(this)

    fun runGame(period: Long = this.period) {
        this.period = period
        if (running) return
        running = true
        timer.scheduleAtFixedRate(gameLoop, 0, period)
    }

    fun stopGame() {
        if (!running) return
        running = false
        gameLoop.cancel()
    }

    fun addTask(task: TimerTask) {
        timer.schedule(task, 0)
    }
}


class GameLoop(private val gameLifeCycle: GameLifeCycle) : TimerTask() {
    override fun run() {
        if (gameLifeCycle.running) {
            gameLifeCycle.addTask(GameTask { Game.tick() })
        }
    }
}

class GameTask(private val func: suspend () -> Unit) : TimerTask() {
    override fun run() {
        runBlocking { func() }
    }
}