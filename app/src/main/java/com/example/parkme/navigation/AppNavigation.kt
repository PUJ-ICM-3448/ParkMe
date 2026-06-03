package com.example.parkme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parkme.data.firebase.FirebaseAuthRepository
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.data.mock.MockReservationData
import com.example.parkme.location.LocationTrackingService
import com.example.parkme.sensors.ParkMeSensorState
import com.example.parkme.ui.screens.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(sensorState: ParkMeSensorState) {

    val navController  = rememberNavController()
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Si Firebase ya tiene sesión activa (la app fue cerrada y reabierta),
    // restauramos el usuario en MockAuth consultando su perfil en la DB.
    // Esto evita que la cuenta quede "pegada" a la primera registrada.
    LaunchedEffect(Unit) {
        if (FirebaseAuthRepository.isLoggedIn() && MockAuth.currentUser == null) {
            val result = FirebaseAuthRepository.fetchCurrentUserProfile()
            result.onSuccess { user ->
                MockAuth.setCurrentUser(user)
                // Navegar a la pantalla correcta según el rol
                if (user.role == "CLIENT") {
                    navController.navigate(Routes.CLIENT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.OWNER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            // Si falla (sin red, etc.) el usuario verá el login normalmente
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                val user = MockAuth.currentUser
                if (user != null && user.role == "CLIENT") {
                    LocationTrackingService.start(context, user.email, user.name)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN)    { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }

        composable(Routes.CLIENT_HOME) {
            ClientHomeScreen(navController = navController, sensorState = sensorState)
        }
        composable(Routes.OWNER_HOME)  { OwnerHomeScreen(navController) }
        composable(Routes.PARKING_LIST){ ParkingListScreen(navController) }
        composable(Routes.SEARCH_ADDRESS) { SearchByAddressScreen(navController) }
        composable(Routes.PROFILE)    { ProfileScreen(navController) }

        composable("${Routes.PARKING_DETAIL}/{parkingId}") { back ->
            val parkingId = back.arguments?.getString("parkingId") ?: ""
            ParkingDetailScreen(navController = navController, parkingId = parkingId)
        }

        composable("${Routes.RESERVATION}/{parkingId}") { back ->
            val parkingId = back.arguments?.getString("parkingId")?.toInt() ?: 0
            val parking   = MockParkingData.getParkingById(parkingId)
            ReservationScreen(navController = navController, parkingId = parkingId, parkingName = parking?.name ?: "")
        }

        composable(Routes.NOTIFICATIONS) { NotificationScreen() }

        composable("${Routes.CHAT}/{parkingId}") { back ->
            val id = back.arguments?.getString("parkingId")?.toInt() ?: 0
            ChatScreen(navController, id)
        }

        composable(Routes.MY_RESERVATIONS) { MyReservationsScreen(navController) }

        composable("${Routes.OWNER_PARKING_DETAIL}/{parkingId}") { back ->
            val id = back.arguments?.getString("parkingId")?.toInt() ?: 0
            OwnerParkingDetailScreen(navController, id)
        }

        composable(Routes.ADD_PARKING) { AddParkingScreen(navController) }
        composable(Routes.SENSORS)     { SensorsScreen(navController) }

        composable("${Routes.ROUTE_MAP}/{destLat}/{destLng}/{parkingName}") { back ->
            val destLat     = back.arguments?.getString("destLat")?.toDoubleOrNull() ?: 0.0
            val destLng     = back.arguments?.getString("destLng")?.toDoubleOrNull() ?: 0.0
            val parkingName = back.arguments?.getString("parkingName") ?: "Parqueadero"
            RouteMapScreen(navController = navController, destLat = destLat, destLng = destLng, parkingName = parkingName)
        }

        composable(Routes.USER_TRACKING) { UserTrackingMapScreen(navController) }

        composable("${Routes.QR_CODE}/{reservationId}") { back ->
            val reservationId = back.arguments?.getString("reservationId")?.toInt() ?: 0
            val reservation   = MockReservationData.getReservationById(reservationId)
            if (reservation != null) {
                QrScreen(navController = navController, reservation = reservation)
            }
        }
    }
}
