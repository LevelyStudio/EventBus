import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import gg.levely.system.eventbus.EventBus
import gg.levely.system.eventbus.EventPriority
import gg.levely.system.eventbus.branch.branch
import gg.levely.system.eventbus.filter.EventFilters
import org.slf4j.LoggerFactory

fun main() {
    val player1 = Player("Alice", 2000.0)
    val player2 = Player("Bob", 1500.0)

    val eventBus = EventBus<GameEvent>()
//    enableEventBusDebug()

    val customBranch = eventBus.branch {
        subscribe<PlayerJoinEvent> { event ->
            println("Player joined: ${event.player}")
        }
    }

    println("Path of custom branch: ${customBranch.getPath()}")

    eventBus.publish(PlayerJoinEvent(player1))
    customBranch.detach()
    eventBus.publish(PlayerJoinEvent(player2))

    val filterHighTransaction = EventFilters.exact<ProcessTransactionEvent>() and EventFilters.filter { event ->
        event.amount > 1_000_000.0
    }

    eventBus.subscribe<ProcessTransactionEvent>(filter = filterHighTransaction) { event ->
        println("Processing high-value transaction of ${event.amount} for ${event.source}")
    }

    eventBus.subscribe<ProcessTransactionEvent>(priority = EventPriority.HIGHEST) { event ->
        println("Processing regular transaction of ${event.amount} for ${event.source}")
    }

    eventBus.publish(ProcessTransactionEvent(player1, player2, 500_000.0))
    eventBus.publish(ProcessTransactionEvent(player1, player2, 2_000_000.0))

    customBranch.reattach()
    eventBus.publish(PlayerJoinEvent(player2))

}

fun enableEventBusDebug() {
    val logger = LoggerFactory.getLogger(EventBus::class.java) as Logger
    logger.level = Level.DEBUG
}