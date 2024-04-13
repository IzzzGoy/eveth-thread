package org.company.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import kotlinx.serialization.encodeToString
import org.company.sample.theme.AppTheme
import ru.alexey.event.threads.LocalScope
import ru.alexey.event.threads.LocalScopeHolder
import ru.alexey.event.threads.ScopeHolder
import ru.alexey.event.threads.navgraph.NavGraph
import ru.alexey.event.threads.resources.invoke
import ru.alexey.event.threads.scopeholder.generateActiveSchema
import ru.alexey.event.threads.scopeholder.generateStaticSchema
import ru.alexey.event.threads.widget.createWidget


val startWidget = createWidget<Int>("StartScreen") { it, modifier ->
    val scope = LocalScope.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
            .clickable {
                scope + SetInt(it + 1)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(it.toString())
    }
}


@Composable
internal fun App() = AppTheme {
    ScopeHolder(::provideScopeHolder) {
        LocalScopeHolder.current.generateStaticSchema().also {
            Logger.a("SCHEMA_STATIC") {
                jsonResource()().encodeToString(
                    it
                )
            }
        }
        LocalScopeHolder.current.generateActiveSchema().also {
            Logger.a("SCHEMA_ACTIVE") {
                jsonResource()().encodeToString(
                    it
                )
            }
        }
        NavGraph("Navigation")
    }
}

internal expect fun openUrl(url: String?)