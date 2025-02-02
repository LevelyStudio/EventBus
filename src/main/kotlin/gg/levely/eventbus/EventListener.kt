package gg.levely.eventbus

fun interface EventListener<in E> {

    fun onEvent(event: E)
}