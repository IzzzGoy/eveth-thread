package ru.alexey.event.threads.datacontainer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import ru.alexey.event.threads.resources.ObservableResource
import kotlin.properties.ReadOnlyProperty


interface Datacontainer<T> : StateFlow<T> {
    suspend fun update(block: (T) -> T)
}

abstract class RealDataContainer<T>(
    stateFlow: StateFlow<T>
) : StateFlow<T> by stateFlow, Datacontainer<T>


@OptIn(ExperimentalStdlibApi::class)
inline fun<reified T: Any> ContainerBuilder.realDataContainer(
    flow: StateFlow<T>, scope: CoroutineScope , crossinline innerUpdate: ((T) -> T) -> Unit
): RealDataContainer<T> = object : AutoCloseable, RealDataContainer<T>(
    flow
) {

    override suspend fun update(block:  (T) -> T) {
        innerUpdate(block)
    }

    init {
        this@realDataContainer[T::class] = this as Datacontainer<T>
        launchIn(scope)
    }

    override fun close() {
        scope.cancel()
    }
}




