package gg.levely.system

fun interface EventListener<in E> {

    fun onEvent(event: E)
}