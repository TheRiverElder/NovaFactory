package io.theriverelder.novafactory.util.task

import kotlinx.coroutines.runBlocking
import java.util.*

class TaskQueue<T> where T : Runnable {
    private val queue: Queue<T> = LinkedList()
    private var inProcess: Boolean = false

    fun enqueueTask(task: T) {
        queue.add(task)
        if (!inProcess) {
            inProcess = true
            runBlocking {
                while (queue.isNotEmpty()) {
                    queue.poll().run()
                }
            }
            inProcess = false
        }
    }
}