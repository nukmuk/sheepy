package me.nukmuk.sheepy.utils

import me.nukmuk.sheepy.AnimationsManager
import me.nukmuk.sheepy.Config
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import kotlin.text.replace

object Utils {
    val mm = MiniMessage.miniMessage()

    fun sendMessage(target: CommandSender, message: String) {
        val text = mm.deserialize("${Config.PLUGIN_PREFIX} <reset>$message")
        target.sendMessage(text)
    }

    fun sendDebugMessage(message: String) {
        val text = mm.deserialize("${Config.PLUGIN_PREFIX} Debug: <reset>$message")
        for (player in Bukkit.getServer().onlinePlayers)
            if (AnimationsManager.debugPlayers.contains(player.uniqueId))
                player.sendMessage(text)
    }

    // from https://stackoverflow.com/a/6162687
    // ignores the higher 16 bits
    fun convertToFloat(hbits: Int): Float {
        var mant = hbits and 0x03ff // 10 bits mantissa
        var exp = hbits and 0x7c00 // 5 bits exponent
        if (exp == 0x7c00)  // NaN/Inf
            exp = 0x3fc00 // -> NaN/Inf
        else if (exp != 0)  // normalized value
        {
            exp += 0x1c000 // exp - 15 + 127
            if (mant == 0 && exp > 0x1c400)  // smooth transition
                return java.lang.Float.intBitsToFloat((hbits and 0x8000) shl 16 or (exp shl 13) or 0x3ff)
        } else if (mant != 0)  // && exp==0 -> subnormal
        {
            exp = 0x1c400 // make it normal
            do {
                mant = mant shl 1 // mantissa * 2
                exp -= 0x400 // decrease exp by 1
            } while ((mant and 0x400) == 0) // while not normal
            mant = mant and 0x3ff // discard subnormal bit
        } // else +/-0 -> +/-0

        return java.lang.Float.intBitsToFloat( // combine all parts
            (hbits and 0x8000) shl 16 // sign  << ( 31 - 15 )
                    or ((exp or mant) shl 13)
        ) // value << ( 23 - 10 )
    }

    fun sanitizeString(s: String?): String? {
        return s?.replace(Regex("[^a-zA-Z0-9_-]"), "")
    }

    fun toRadians(degrees: Float): Float {
        return degrees * (Math.PI.toFloat() / 180f)
    }

    fun toDegrees(radians: Float): Float {
        return radians * (180f / Math.PI.toFloat())
    }
}