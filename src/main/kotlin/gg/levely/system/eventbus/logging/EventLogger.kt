package gg.levely.system.eventbus.logging

import gg.levely.system.eventbus.EventBus
import org.slf4j.LoggerFactory

class EventLogger {

    private val logger = LoggerFactory.getLogger(EventBus::class.java)

    fun <E> logEvent(eventType: EventType, event: Class<E>, eventListener: Any? = null) {
        when (eventType) {
            EventType.PUBLISH ->
                logger.debug("[{}] Event: {}", eventType, event.simpleName)

            EventType.SUBSCRIBE, EventType.UNSUBSCRIBE -> {
                eventListener?.let {
                    logger.debug(
                        "[{}] Listener: {} -> Event: {}",
                        eventType,
                        it.javaClass.simpleName,
                        event.simpleName
                    )
                }
            }
        }
    }

    fun isDebugEnabled(): Boolean {
        return logger.isDebugEnabled
    }

}