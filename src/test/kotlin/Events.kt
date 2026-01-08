data class Player(val name: String, val balance: Double)
interface GameEvent


data class PlayerJoinEvent(val player: Player) : GameEvent
data class PlayerLeaveEvent(val player: Player) : GameEvent

interface TransactionEvent : GameEvent {

}

data class ProcessTransactionEvent(
    val source: Player,
    val target: Player,
    val amount: Double
) : TransactionEvent