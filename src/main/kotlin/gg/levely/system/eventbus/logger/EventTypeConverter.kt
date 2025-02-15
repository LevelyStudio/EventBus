package gg.levely.system.eventbus.logger

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.CompositeConverter

class EventTypeConverter : CompositeConverter<ILoggingEvent>() {

    override fun transform(event: ILoggingEvent, value: String): String {
        val eventType = event.mdcPropertyMap["eventType"]
        return eventType ?: value
    }
}