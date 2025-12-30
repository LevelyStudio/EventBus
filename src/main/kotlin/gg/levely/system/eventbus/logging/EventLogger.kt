package gg.levely.system.eventbus.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.CompositeConverter
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class EventLogger(name: String = "EventBus") {

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

class EventTypeConverter : CompositeConverter<ILoggingEvent>() {

    override fun transform(event: ILoggingEvent, value: String): String {
        val eventType = event.mdcPropertyMap["eventType"]
        return eventType ?: value
    }

}