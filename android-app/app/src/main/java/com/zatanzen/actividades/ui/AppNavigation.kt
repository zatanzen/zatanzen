package com.zatanzen.actividades.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zatanzen.actividades.ui.create.CreateActivityScreen
import com.zatanzen.actividades.ui.detail.ActivityDetailScreen
import com.zatanzen.actividades.ui.map.MapScreen
import com.zatanzen.actividades.ui.profile.ProfileScreen

@Composable
fun AppNavigation(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "map") {
        composable("map") {
            MapScreen(
                vm = vm,
                onActivityClick = { nav.navigate("activity/$it") },
                onCreateClick = { nav.navigate("create") },
                onProfileClick = { nav.navigate("profile/${vm.repo.currentUserId}") },
            )
        }
        composable("activity/{id}") { entry ->
            ActivityDetailScreen(
                vm = vm,
                activityId = entry.arguments?.getString("id").orEmpty(),
                onBack = { nav.popBackStack() },
                onUserClick = { nav.navigate("profile/$it") },
            )
        }
        composable("create") {
            CreateActivityScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onCreated = { id ->
                    nav.popBackStack()
                    nav.navigate("activity/$id")
                },
            )
        }
        composable("profile/{userId}") { entry ->
            ProfileScreen(
                vm = vm,
                userId = entry.arguments?.getString("userId").orEmpty(),
                onBack = { nav.popBackStack() },
                onActivityClick = { nav.navigate("activity/$it") },
            )
        }
    }
}
