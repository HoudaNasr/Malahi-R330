package com.rpay.v2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationManager @Inject constructor() {
    private val navigationCommands = Channel<NavigationCommand>(capacity = Channel.UNLIMITED)
    val commands = navigationCommands.receiveAsFlow()

    private val coroutineScope = MainScope()

    fun navigate(command: NavigationCommand) {
        coroutineScope.launch {
            navigationCommands.send(command)
        }
    }
}

sealed class NavigationCommand {
    data class NavigateToRoute(val route: String, val options: NavOptions? = null) : NavigationCommand()
    data class NavigateUp(val route: String? = null) : NavigationCommand()
    object PopBackStack : NavigationCommand()
    data class PopUpTo(val route: String, val inclusive: Boolean) : NavigationCommand()
}

@Composable
fun NavigationHandler(
    navController: NavHostController,
    navigationManager: NavigationManager
) {
    LaunchedEffect(Unit) {
        navigationManager.commands.collect { command ->
            when (command) {
                is NavigationCommand.NavigateToRoute -> {
                    navController.navigate(command.route, command.options)
                }
                is NavigationCommand.NavigateUp -> {
                    navController.navigateUp()
                }
                NavigationCommand.PopBackStack -> {
                    navController.popBackStack()
                }
                is NavigationCommand.PopUpTo -> {
                    navController.popBackStack(command.route, command.inclusive)
                }
            }
        }
    }
}