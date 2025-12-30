package gg.levely.system.eventbus

interface BranchCreator<T> {

    fun branch(name: String): EventBranch<T>

    fun branch(): EventBranch<T> =
        branch(generateBranchName())

    companion object {

        @JvmStatic
        fun generateBranchName(): String =
            "branch-" + System.currentTimeMillis().toString(16) + "-" + (Math.random() * 0x10000).toInt().toString(16)

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