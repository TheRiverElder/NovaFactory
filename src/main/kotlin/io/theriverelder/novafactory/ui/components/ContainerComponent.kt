package io.theriverelder.novafactory.ui.components

interface ContainerComponent : Component {

    fun add(component: NonRootComponent)

}


typealias Initiator<E> = E.() -> Unit
typealias ContainerInitiator = Initiator<ContainerComponent>