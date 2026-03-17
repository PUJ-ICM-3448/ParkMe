package com.example.parkme.data.mock

import com.example.parkme.data.model.Parking

object MockParkingData {

    val parkingList = mutableListOf(

        Parking(1, "Parking Centro", "Calle 10 #5-20", 5000.0, "owner@mail.com", 12, 4),
        Parking(2, "Parking Plaza", "Carrera 7 #30-12", 6000.0, "owner@mail.com", 15, 6),
        Parking(3, "Parking Universidad", "Calle 45 #15-10", 4500.0, "owner@mail.com", 20, 8),
        Parking(4, "Parking Zona Rosa", "Carrera 13 #85-40", 7000.0, "owner@mail.com", 10, 3),

        Parking(5, "Parking Chapinero", "Carrera 13 #60-45", 5500.0, "owner@mail.com", 8, 2),
        Parking(6, "Parking Andino", "Calle 82 #12-18", 8000.0, "owner@mail.com", 12, 5),
        Parking(7, "Parking Salitre", "Avenida 68 #24-50", 4800.0, "owner@mail.com", 18, 7),
        Parking(8, "Parking Aeropuerto", "Avenida El Dorado #103-09", 9000.0, "owner@mail.com", 25, 10),

        Parking(9, "Parking Parque 93", "Calle 93 #11-45", 7500.0, "owner@mail.com", 14, 4),
        Parking(10, "Parking Unicentro", "Carrera 15 #124-30", 6500.0, "owner@mail.com", 20, 9),
        Parking(11, "Parking Santa Fe", "Calle 185 #45-03", 5500.0, "owner@mail.com", 30, 12),
        Parking(12, "Parking Gran Estación", "Calle 26 #62-47", 6200.0, "owner@mail.com", 10, 3),

        Parking(13, "Parking Teusaquillo", "Carrera 24 #39-18", 4200.0, "owner@mail.com", 12, 5),
        Parking(14, "Parking Galerías", "Calle 53 #27-10", 4300.0, "owner@mail.com", 16, 6),
        Parking(15, "Parking Modelia", "Carrera 82 #24-10", 4100.0, "owner@mail.com", 10, 4),
        Parking(16, "Parking Hayuelos", "Calle 20 #82-52", 4700.0, "owner@mail.com", 18, 7),

        Parking(17, "Parking Suba Centro", "Calle 146 #91-10", 3900.0, "owner@mail.com", 20, 8),
        Parking(18, "Parking Portal Norte", "Autopista Norte #174-25", 5200.0, "owner@mail.com", 25, 9),
        Parking(19, "Parking Usaquén", "Carrera 7 #119-14", 7100.0, "owner@mail.com", 15, 5),
        Parking(20, "Parking La Candelaria", "Calle 12B #2-45", 4500.0, "owner@mail.com", 12, 3)

    )

    fun addParking(parking: Parking) {
        parkingList.add(parking)
    }

    fun getParkingById(id: Int): Parking? {
        return parkingList.find { it.id == id }
    }
}