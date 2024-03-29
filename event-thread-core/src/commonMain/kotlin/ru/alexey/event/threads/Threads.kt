package ru.alexey.event.threads

import ru.alexey.event.threads.utils.Builder

open class EventThread<T: Event>(
    private val metadata: EventMetadata
) {
    private val eventThreadActions: MutableList<EventThreadAction<T>> = mutableListOf()

    val actions: List<suspend (Event) -> Unit>
        get() = eventThreadActions.map { it.action as suspend (Event) -> Unit }

    val eventMetadatas: EventThreadInfo
        get() = EventThreadInfo(metadata, eventThreadActions.map { it.type })

    operator fun invoke(eventThreadAction: EventThreadAction<T>) {
        eventThreadActions.add(
            eventThreadAction
        )
    }
}

data class EventThreadInfo(
    val metadata: EventMetadata,
    val actions: List<EventType>
)

enum class EventType {
    consume, cascade, process, modification, external
}

enum class Privacy {
    private, public
}

class EventThreadAction<T: Event>(
    val action: suspend (T) -> Unit,
    val type: EventType
)

data class EventMetadata(
    val description: String,
    val privacy: Privacy = Privacy.public
)

class EventThreadMetadataBuilder<T: Event>(
    private var description: String = "",
    private var privacy: Privacy = Privacy.public
): Builder<EventThread<T>> {



    override fun build(): EventThread<T> {
        return EventThread<T>(
            EventMetadata(
                description, privacy
            )
        )
    }

    fun description(block: () -> String) {
        description = block()
    }

    fun privacy(privacy: Privacy) {
        this.privacy = privacy
    }
}

class EventThreadActionBuilder<T: Event>(
    private val type: EventType,
    private val action: suspend (T) -> Unit
): Builder<EventThreadAction<T>> {
    override fun build(): EventThreadAction<T> {
        return EventThreadAction(action, type)
    }
}







