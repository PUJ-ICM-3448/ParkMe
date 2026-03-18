package com.example.parkme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.parkme.data.mock.MockParkingData
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
            ProfileScreen(navController)
        }

        composable("${Routes.PARKING_DETAIL}/{parkingId}") { backStackEntry ->

            val parkingId =
                backStackEntry.arguments?.getString("parkingId") ?: ""

            ParkingDetailScreen(
                navController = navController,
                parkingId = parkingId
            )
        }

        composable("${Routes.RESERVATION}/{parkingId}") { backStackEntry ->

            val parkingId =
                backStackEntry.arguments?.getString("parkingId")?.toInt() ?: 0

            val parking =
                MockParkingData.getParkingById(parkingId)

            ReservationScreen(
                navController = navController,
                parkingId = parkingId,
                parkingName = parking?.name ?: ""
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationScreen()
        }

        composable("${Routes.CHAT}/{parkingId}") { backStackEntry ->

            val id = backStackEntry.arguments?.getString("parkingId")?.toInt() ?: 0

            ChatScreen(navController, id)
        }

        composable(Routes.MY_RESERVATIONS) {
            MyReservationsScreen(navController)
        }

        composable("${Routes.OWNER_PARKING_DETAIL}/{parkingId}") { backStackEntry ->

            val id = backStackEntry.arguments?.getString("parkingId")?.toInt() ?: 0

            OwnerParkingDetailScreen(navController, id)
        }

        composable(Routes.ADD_PARKING) {
            AddParkingScreen(navController)
        }

    }

}