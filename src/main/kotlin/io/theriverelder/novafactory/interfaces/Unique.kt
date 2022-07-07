package io.theriverelder.novafactory.interfaces

interface Unique<U> {
    val uid: U
}

fun <U, E : Unique<U>> Iterable<E>.findIndexByUid(uid: U): Int = indexOfFirst { it.uid == uid }

fun <U, E : Unique<U>> Iterable<E>.findByUid(uid: U): E =
    find { it.uid == uid } ?: throw Exception("Cannot find element with uid: $uid")

fun <U, E : Unique<U>> Iterable<E>.tryFindByUid(uid: U): E? = find { it.uid == uid }

fun <U, E : Unique<U>> MutableIterable<E>.removeByUid(uid: U): Boolean = removeAll { it.uid == uid }