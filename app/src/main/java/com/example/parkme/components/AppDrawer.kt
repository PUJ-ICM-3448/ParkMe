package com.example.parkme.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.navigation.Routes

@Composable
fun AppDrawer(
    navController: NavController,
    content: @Composable () -> Unit
) {

    ModalNavigationDrawer(

        drawerContent = {

            ModalDrawerSheet {

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ParkMe Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.CLIENT_HOME)
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.PROFILE)
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Change Account") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.LOGIN)
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {

                        MockAuth.logout()

                        navController.navigate(Routes.LOGIN)

                    }
                )

            }

        }

    ) {

        content()

    }

}