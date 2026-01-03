package gg.levely.system.eventbus.branch

import gg.levely.system.eventbus.EventBus
import gg.levely.system.eventbus.EventListener
import gg.levely.system.eventbus.EventPriority
import gg.levely.system.eventbus.filter.EventFilter
import gg.levely.system.eventbus.EventContext

internal class DefaultEventBranch<T>(
    private val name: String,
    private val root: EventBus<T>,
    private val parent: EventBranch<T>?
) : EventBranch<T>() {

    private val children = mutableListOf<EventBranch<T>>()
    private val contexts = mutableListOf<EventContext<*>>()
    private var status: BranchStatus = BranchStatus.ATTACHED

    override fun getName(): String = name

    override fun getChildren(): List<EventBranch<T>> = children.toList()

    override fun getPath(): String {
        return parent?.let { "${it.getPath()}/$name" } ?: name
    }

    override fun getStatus(): BranchStatus = status

    override fun <E : T> subscribe(
        eventType: Class<E>,
        listener: EventListener<E>,
        priority: EventPriority,
        filter: EventFilter<E>
    ): EventContext<E> {
        val context = root.subscribe(eventType, listener, priority, filter)
        contexts.add(context)
        return context
    }

    override fun <E : T> publish(event: E): E {
        return root.publish(event)
    }

    override fun branch(name: String): EventBranch<T> {
        val childBranch = DefaultEventBranch(name, root, this)
        children.add(childBranch)
        return childBranch
    }

    override fun detach() {
        if (status == BranchStatus.DETACHED) return
        status = BranchStatus.DETACHED

        contexts.forEach { context ->
            root.unsubscribe(context as EventContext<Any>)
        }

        children.forEach { child ->
            child.detach()
        }

    }

    override fun reattach() {
        if (status == BranchStatus.ATTACHED) return
        status = BranchStatus.ATTACHED

        contexts.forEach { context ->
            root.subscribe(context as EventContext<Any>)
        }
    }


}