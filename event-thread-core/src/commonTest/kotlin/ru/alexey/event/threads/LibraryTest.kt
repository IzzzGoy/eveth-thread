import kotlinx.coroutines.test.runTest
import ru.alexey.event.threads.Privacy
import ru.alexey.event.threads.StrictEvent
import ru.alexey.event.threads.scopeBuilder
import kotlin.test.Test


data object DummyEvent : StrictEvent
data object DummyEvent2 : StrictEvent

class Test {


    @Test
    fun test() = runTest {
        val scope = scopeBuilder("test") {

            threads {
                thread<DummyEvent> {
                    description {
                        "Just a test event"
                    }
                    privacy(Privacy.private)
                }.then {
                    DummyEvent2
                }.end {
                    println("!!!!!!!!!!!!!!!!!!!!!!!")
                }
                thread<DummyEvent2>().end {
                    println("@@@@@@@@@@@@@@@@@@@@@@@@@")
                }

            }
        }

        println(scope.metadata)
        scope + DummyEvent
    }
}