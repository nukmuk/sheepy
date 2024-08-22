package me.nukmuk.sheepy

object Config {
    const val FILE_EXTENSION = "shny"
    const val VAR_COLOR = "<gray>"
    const val PRIMARY_COLOR = "<white>"
    const val ERROR_COLOR = "<red>"
    val PLUGIN_NAME_COLORS = "<gradient:#e64ffe:#ff3dd4>${Sheepy::class.simpleName}</gradient>"
    val PLUGIN_PREFIX = "<gray>[$PLUGIN_NAME_COLORS<gray>]"

    object Strings {
        const val NO_PERMISSION = "${ERROR_COLOR}You don't have permissions to this command"
    }
}