package io.theriverelder.novafactory.ui.components

abstract class NonRootComponent : Component {
    var parent: Component? = null
}