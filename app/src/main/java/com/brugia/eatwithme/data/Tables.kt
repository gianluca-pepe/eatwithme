
package com.brugia.eatwithme.data

import android.content.res.Resources
import com.brugia.eatwithme.R


/* Returns initial list of tables. */
fun tableList(resources: Resources): List<Table> {
    return listOf(
            Table(
                    id = 1,
                    name = "Pranzo conoscitivo a Roma centro",
                    image = R.drawable.logo_login,
                    description = "Mangiamo in compagnia per le vie centrali della capitale",
                    maxPartecipants = 10,
                    numPartecipants = 3,
                    tableDate = "2020/12/13",
                    tableHour = "13:30",
                    city = "Roma"
            ),
            Table(
                    id = 2,
                    name = "Pranzo pendolari Roma Termini",
                    image = R.drawable.logo_login,
                    description = "Mangiamo in compagnia alla stazione principale di Roma",
                    maxPartecipants = 4,
                    numPartecipants = 2,
                    tableDate = "2020/12/14",
                    tableHour = "12:45",
                    city = "Velletri",
            ),
            Table(
                    id = 3,
                    name = "Pranzo Fontana di Trevi",
                    image = R.drawable.logo_login,
                    description = "Mangiamo davanti alla Fontana di Trevi",
                    maxPartecipants = 8,
                    numPartecipants = 7,
                    tableDate = "2020/12/14",
                    tableHour = "13:15",
                    city = "Latina",
            ),
            Table(
                    id = 4,
                    name = "Cena a Villa Borghese (RM)",
                    image = R.drawable.logo_login,
                    description = "Ceniamo nei pressi di Villa Borghese e poi facciamo due passi",
                    maxPartecipants = 12,
                    numPartecipants = 5,
                    tableDate = "2020/12/14",
                    tableHour = "20:00",
                    city = "Aprilia",
            ),
            Table(
                    id = 5,
                    name = "Cena zona EUR",
                    image = R.drawable.logo_login,
                    description = "Ceniamo all'EUR e poi facciamo due passi nel parchetto",
                    maxPartecipants = 12,
                    numPartecipants = 5,
                    tableDate = "2020/12/14",
                    tableHour = "20:10",
                    city = "Firenze"
            ),
            Table(
                    id = 6,
                    name = "Un panino al volo",
                    image = R.drawable.logo_login,
                    description = "Hai la pausa pranzo e vuoi mangiare in compagnia? Unisciti a noi!",
                    maxPartecipants = 4,
                    numPartecipants = 1,
                    tableDate = "2020/12/16",
                    tableHour = "13:15",
                    city = "Milano"
            )
    )
}


