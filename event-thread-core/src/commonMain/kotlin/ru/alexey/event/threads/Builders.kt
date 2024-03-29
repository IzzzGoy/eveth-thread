package ru.alexey.event.threads

import ru.alexey.event.threads.EventBus.Companion.defaultFactory
import ru.alexey.event.threads.datacontainer.Datacontainer
import ru.alexey.event.threads.scopeholder.KeyHolder
import ru.alexey.event.threads.datacontainer.ContainerBuilder
import ru.alexey.event.threads.emitter.Emitter
import ru.alexey.event.threads.emitter.EmittersBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class ScopeBuilder(
    private var name: String,
) {


    val scope: Scope
            by lazy {
                with(ConfigBuilder()){

                    configs()

                    object : Scope() {
                        override val key: String = name
                        override val eventBus: EventBus = this@with()
                        override fun <T : Any> get(clazz: KClass<T>): Datacontainer<T>? = containerBuilder[clazz]

                        init {
                            applied.forEach {
                                it()
                            }
                            emitters = emittersBuilder.build(this)
                        }
                    }.apply {
                        containerBuilder.mutex.unlock()
                    }
                }
            }

    private var configs: ConfigBuilder.() -> Unit = {}
    val containerBuilder = ContainerBuilder()
    private val applied = mutableListOf<Scope.() -> Unit>()
    private val emittersBuilder = EmittersBuilder()

    @Builder
    fun config(block: ConfigBuilder.() -> Unit) {
        configs = block
    }

    @Builder
    fun containers(block: ContainerBuilder.() -> Unit) {
        containerBuilder.apply(block)
    }

    @Builder
    fun threads(block: Scope.() -> Unit) {
        applied.add { block() }
    }

    @Builder
    fun name(block: () -> String) {
        name = block()
    }

    @Builder
    fun emitters(block: EmittersBuilder.() -> Unit) {
        emittersBuilder.apply(block)
    }
}

class ConfigBuilder {
    private var eventBus: EventBus = defaultFactory()
    @Builder
    fun createEventBus(block: EventBussBuilder.() -> Unit) {
        eventBus = with(EventBussBuilder().also(block)) { build() }
    }

    operator fun invoke() = eventBus
}


abstract class Scope() : KeyHolder {

    abstract val eventBus: EventBus
    val metadata
        get() = eventBus.metadata
    protected lateinit var emitters: List<Emitter<out Event>>
    abstract operator fun <T : Any> get(clazz: KClass<T>): Datacontainer<T>?
    inline fun <reified T : Any> resolve(): Datacontainer<T>? = get(T::class)
    inline fun <reified T : Any> resolveOrThrow(): Datacontainer<T> =
        get(T::class) ?: throw Exception("Container not registered")

    inline operator fun<reified T: Any> getValue(thisRef: Any?, property: KProperty<*>): Datacontainer<T> {
        return resolveOrThrow()
    }

    operator fun plus(event: Event) = eventBus + event


    inline fun <reified T : Event> thread(block: EventThreadMetadataBuilder<T>.() -> Unit): EventThread<T> {
        return EventThreadMetadataBuilder<T>().apply(block).build().also { eventBus { it } }
    }

    inline fun <reified T : Event> thread(): EventThread<T> {
        return EventThreadMetadataBuilder<T>().build().also { eventBus { it } }
    }

    @Builder
    inline infix fun <reified T : Event, reified OTHER : Event> EventThread<T>.then(
        crossinline factory: suspend (T) -> OTHER
    ): EventThread<T> {
        val action = EventThreadActionBuilder<T>(EventType.cascade) {
            eventBus + factory(it)
        }
        invoke(action.build())
        return this
    }

    @Builder
    inline fun <reified T : Event, reified TYPE : Any> EventThread<T>.then(
        datacontainer: Datacontainer<TYPE>,
        crossinline factory: suspend (TYPE, T) -> TYPE
    ): EventThread<T> {
        val action = EventThreadActionBuilder<T>(EventType.modification) {
            val new = factory(datacontainer.value, it)
            datacontainer.update {
                new
            }
        }
        invoke(action.build())
        return this
    }

    @Builder
    inline infix fun <reified T : Event> EventThread<T>.end(
        crossinline block: suspend (T) -> Unit
    ): EventThread<T> {
        val action = EventThreadActionBuilder<T>(EventType.consume) {
            block(it)
        }
        invoke(action.build())
        return this
    }

    @Deprecated("Use thread instead", ReplaceWith("thread<T>()"))
    @Builder
    inline fun <reified T : Event> eventThread(): EventThread<T> {

        return  EventThread<T>(
            EventMetadata("", Privacy.public)
        ).also {
            eventBus { it }
        }
    }

}

@Builder
fun scopeBuilder(keyHolder: KeyHolder? = null, block: ScopeBuilder.() -> Unit): Scope =
    scopeBuilder(keyHolder?.key, block)

@Builder
fun scopeBuilder(name: String? = null, block: ScopeBuilder.() -> Unit): Scope {
    val scope = ScopeBuilder(
        name ?: Random.Default.nextBytes(132).toString()
    ).apply { block() }

    return scope.scope
}

@DslMarker
annotation class Builder

