package com.lura.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lura.ui.library.LibraryScreen
import com.lura.ui.reader.ReaderScreen

object Routes {
    const val LIBRARY = "library"
    const val READER = "reader/{bookId}"
    
    fun reader(bookId: String) = "reader/$bookId"
}

@Composable
fun LuraNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LIBRARY
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onBookClick = { bookId ->
                    navController.navigate(Routes.reader(bookId))
                }
            )
        }
        composable(Routes.READER) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            ReaderScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
