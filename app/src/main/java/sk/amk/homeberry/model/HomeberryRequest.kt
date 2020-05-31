package sk.amk.homeberry.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a request loaded from configuration
 *
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
@Entity
data class HomeberryRequest(
    var name: String = "",
    var endpoint: String = "",
    var openApp: Boolean = false,
    var openAppPackageName: String = ""
) {

    @PrimaryKey
    var id: Long = 0
}
