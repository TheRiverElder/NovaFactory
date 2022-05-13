package io.theriverelder.novafactory.util.event

class EventDispatcher<E> {
    private val handlers: MutableSet<EventHandler<E>> = HashSet()

    fun add(handler: EventHandler<E>): Boolean = handlers.add(handler)

    fun remove(handler: EventHandler<E>): Boolean = handlers.remove(handler)

    fun clear() = handlers.clear()

    fun emit(event: E) {
        for (handler in handlers) {
            handler.handle(event)
        }
    }
}