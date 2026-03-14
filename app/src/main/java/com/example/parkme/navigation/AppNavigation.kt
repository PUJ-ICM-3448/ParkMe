package com.example.parkme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.parkme.ui.screens.*

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController)
        }

        composable(Routes.CLIENT_HOME) {
            ClientHomeScreen(navController)
        }

        composable(Routes.OWNER_HOME) {
            OwnerHomeScreen(navController)
        }

        composable(Routes.PARKING_LIST) {
            ParkingListScreen(navController)
        }

        composable(Routes.SEARCH_ADDRESS) {
            SearchByAddressScreen(navController)
        }

        composable(Routes.PROFILE) {
            ProfileScreen()
        }

        composable(
            "${Routes.PARKING_DETAIL}/{parkingId}"
        ) { backStackEntry ->

            val parkingId = backStackEntry.arguments?.getString("parkingId") ?: ""

            ParkingDetailScreen(
                navController = navController,
                parkingId = parkingId
            )

        }

    }

}

