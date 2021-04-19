package com.samurainomichi.cloud_storage_client.util

class Observable<T>() {
    constructor(observable: Observable<T>) : this() {
        observable.observe(::invoke)
    }

    private val listeners: MutableList<(T) -> Unit> = mutableListOf()

    fun invoke(value: T) {
        listeners.forEach {
            it.invoke(value)
        }
    }

    fun observe(fn: (T) -> Unit) {
        listeners.add(fn)
    }
}