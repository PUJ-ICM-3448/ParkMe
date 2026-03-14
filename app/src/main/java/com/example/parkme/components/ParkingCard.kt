package com.example.parkme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.parkme.data.model.Parking

@Composable
fun ParkingCard(
    parking: Parking,
    onReserveClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row {

                // Icon container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = "Parking",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = parking.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Dirección",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = parking.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }

                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price chip
            AssistChip(
                onClick = {},
                label = {
                    Text("${parking.pricePerHour} COP / hora")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReserveClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Reservar")

            }

        }

    }

}