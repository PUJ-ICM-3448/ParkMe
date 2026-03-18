package com.example.parkme.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.R
import com.example.parkme.navigation.Routes
import com.example.parkme.ui.components.AppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(navController: NavController) {

    AppDrawer(navController) {

        Scaffold(

            topBar = {
                TopAppBar(

                    title = {
                        Text(
                            "ParkMe",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },

                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate(Routes.PROFILE)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },

                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Routes.NOTIFICATIONS)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    },


                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )

                )
            }

        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {

                MapPlaceholder()

                HomeBottomSheet(
                    navController = navController,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

            }

        }

    }

}

@Composable
fun MapPlaceholder() {

    Image(
        painter = painterResource(id = R.drawable.mapa_provisional),
        contentDescription = "Mapa",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

}

@Composable
fun HomeBottomSheet(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth(),


        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),

        elevation = CardDefaults.cardElevation(8.dp),

        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {


            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Parqueaderos cerca",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Encuentra y reserva fácilmente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN PRINCIPAL
            Button(
                onClick = {
                    navController.navigate(Routes.PARKING_LIST)
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(Icons.Default.Search, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Buscar parqueadero")

            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTÓN SECUNDARIO
            OutlinedButton(
                onClick = {
                    navController.navigate(Routes.SEARCH_ADDRESS)
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(Icons.Default.Place, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Buscar por dirección")

            }

            Spacer(modifier = Modifier.height(12.dp))

            // RESERVAS
            Button(
                onClick = {
                    navController.navigate(Routes.MY_RESERVATIONS)
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(Icons.Default.List, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Ver mis reservas")

            }

        }

    }

}