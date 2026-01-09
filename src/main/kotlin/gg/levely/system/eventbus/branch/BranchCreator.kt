package gg.levely.system.eventbus.branch

import java.util.*

interface BranchCreator<T> {

    fun branch(name: String): EventBranch<T>

    fun branch(): EventBranch<T> =
        branch(generateBranchName())

    companion object {

        @JvmStatic
        fun generateBranchName(): String =
            "branch-" + UUID.randomUUID().toString()
                .replace("-", "")
                .take(8)

    }
}

inline fun <T> BranchCreator<T>.branch(block: EventBranch<T>.() -> Unit): EventBranch<T> {
    val branch = branch()
    branch.block()
    return branch
}

inline fun <T> BranchCreator<T>.branch(name: String, block: EventBranch<T>.() -> Unit): EventBranch<T> {
    val branch = branch(name)
    branch.block()
    return branch
}