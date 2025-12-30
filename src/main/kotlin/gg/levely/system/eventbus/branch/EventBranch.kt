package gg.levely.system.eventbus.branch

import gg.levely.system.eventbus.EventBroker

abstract class EventBranch<T> : EventBroker<T>(), BranchCreator<T>, Detachable {

    abstract fun getName(): String

    abstract fun getChildren(): List<EventBranch<T>>

    abstract fun getPath(): String

    abstract fun getStatus(): BranchStatus

}