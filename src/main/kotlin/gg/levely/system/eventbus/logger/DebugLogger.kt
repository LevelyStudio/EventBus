package gg.levely.system.eventbus.logger

import org.slf4j.LoggerFactory
import org.slf4j.MDC

class DebugLogger(name: String = "EventBus") {

    private val logger = LoggerFactory.getLogger(name)


    fun <E> logEvent(eventType: EventType, event: Class<E>, eventListener: Any? = null) {
        logEventType(eventType) {
            when (eventType) {
                EventType.PUBLISH, EventType.PUBLISH_ASYNC -> logger.info("Event: {}", event.simpleName)
                EventType.SUBSCRIBE, EventType.UNSUBSCRIBE -> {
                    eventListener?.let {
                        logger.info(
                            "Event: {}, Listener: {}",
                            it.javaClass.simpleName,
                            event.simpleName
                        )
                    }
                }
            }
        }
    }


    private fun logEventType(eventType: EventType, loggerAction: () -> Unit) {
        MDC.put("eventType", eventType.name)
        loggerAction.invoke()
        MDC.clear()
    }
}