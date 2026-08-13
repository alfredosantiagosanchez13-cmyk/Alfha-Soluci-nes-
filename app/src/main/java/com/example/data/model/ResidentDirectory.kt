package com.example.data.model

data class ResidentContact(
    val houseNumber: Int,
    val name: String,
    val phone: String
)

object ResidentDirectory {
    val directory = mapOf(
        1 to listOf(ResidentContact(1, "Viridiana Martinez Bolivar", "9933473150")),
        2 to listOf(
            ResidentContact(2, "Georgina Castro", "4421286457"),
            ResidentContact(2, "Fermin Ruiz Rangel", "4422075912")
        ),
        3 to listOf(
            ResidentContact(3, "Jacob Lee", "3344333466"),
            ResidentContact(3, "Sara Kim", "3344627369")
        ),
        4 to listOf(
            ResidentContact(4, "Michael Mould Urías", "4421732234"),
            ResidentContact(4, "Patricia Palacios Sámano", "4421731488")
        ),
        5 to listOf(ResidentContact(5, "Thelma Flores", "4423225204")),
        6 to listOf(
            ResidentContact(6, "Martha Elena Padrón", "4422199249"),
            ResidentContact(6, "Francisco Mendivil", "4421212144")
        ),
        7 to listOf(
            ResidentContact(7, "Rene Roman", "4421942195"),
            ResidentContact(7, "Margarita Pacheco Romero", "4421544127")
        ),
        8 to listOf(
            ResidentContact(8, "Andrea Corona", "4421228785"),
            ResidentContact(8, "Daniela Quiroz", "4422087044")
        ),
        9 to listOf(
            ResidentContact(9, "Marcela Garcia Balderas", "4421281685"),
            ResidentContact(9, "Pablo Arriaga", "4422638380")
        ),
        10 to listOf(ResidentContact(10, "Sofía Muñoz Osorio", "4423233507")),
        11 to listOf(
            ResidentContact(11, "Jesús Alejandro Ramos", "4422650393"),
            ResidentContact(11, "Sofia Ramos", "4424798318")
        ),
        12 to listOf(
            ResidentContact(12, "Gisela Contreras Cervantes", "4423234610"),
            ResidentContact(12, "Rodolfo Tarango Juarez", "4426153883")
        ),
        13 to listOf(
            ResidentContact(13, "Isabella", "5516288674"),
            ResidentContact(13, "Raul Cortes", "4424438263")
        ),
        14 to listOf(
            ResidentContact(14, "Juana (Chuy)", "7531022801"),
            ResidentContact(14, "Liliana", "7531049707")
        ),
        15 to listOf(
            ResidentContact(15, "Alejandra Zentella", "5533338897"),
            ResidentContact(15, "Andrés Cervantes", "5535007591")
        ),
        16 to listOf(
            ResidentContact(16, "Enrique A. Cantoral", "4421526695"),
            ResidentContact(16, "Alma A. Angeles", "4424370898"),
            ResidentContact(16, "Ramiro Cantoral", "4428233133")
        ),
        17 to listOf(ResidentContact(17, "Cecilia Espinosa Villareal", "4422390848")),
        18 to listOf(
            ResidentContact(18, "Oscar Enrique Ramírez", "5544798924"),
            ResidentContact(18, "Claudia Nuñez Real", "4422007447")
        ),
        19 to listOf(ResidentContact(19, "Gabriela Mondragon", "4424468794")),
        20 to listOf(ResidentContact(20, "Raúl Hernandez Saguero", "4425046156")),
        21 to listOf(ResidentContact(21, "Claudia Mireles Viveros", "4421867108")),
        22 to listOf(ResidentContact(22, "Rodolfo Anaya", "5554556583")),
        23 to listOf(
            ResidentContact(23, "Carlos Sánchez", "4422745293"),
            ResidentContact(23, "Vanessa Cuevas", "4423175138")
        ),
        24 to listOf(ResidentContact(24, "Karina Villalobos", "2221580635")),
        25 to listOf(
            ResidentContact(25, "Vianey Desachi Cortes", "4423861833"),
            ResidentContact(25, "Ariel Cuevas", "4421525792")
        ),
        26 to listOf(
            ResidentContact(26, "José Domingo Vargas", "4423443088"),
            ResidentContact(26, "Sara Dorantes Hernández", "4421226911")
        ),
        27 to listOf(ResidentContact(27, "Patricia Ochoa", "4421210131")),
        28 to listOf(
            ResidentContact(28, "Renata Hernandez", "4721486638"),
            ResidentContact(28, "Perla Rubi Acosta", "4462895265")
        ),
        29 to listOf(ResidentContact(29, "María Elena Silva", "5521429256")),
        30 to listOf(ResidentContact(30, "Jorge", "3310478087")),
        31 to listOf(
            ResidentContact(31, "Regina Ojeda", "4623048037"),
            ResidentContact(31, "Luis Gerardo Ojeda Rodriguez", "4423827111")
        ),
        32 to listOf(ResidentContact(32, "Beatriz Eugenia Velázquez", "4421860918"))
    )

    fun findContactByHouse(rawHouseStr: String): ResidentContact? {
        val numberDigits = rawHouseStr.filter { it.isDigit() }
        if (numberDigits.isNotEmpty()) {
            val houseNum = numberDigits.toIntOrNull()
            if (houseNum != null && directory.containsKey(houseNum)) {
                return directory[houseNum]?.firstOrNull()
            }
        }
        return null
    }

    fun findContactByName(name: String): ResidentContact? {
        if (name.length < 3) return null
        val lower = name.lowercase()
        return directory.values.flatten().find {
            it.name.lowercase().contains(lower) || lower.contains(it.name.lowercase())
        }
    }
}
