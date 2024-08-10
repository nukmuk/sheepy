package me.nukmuk.sheepy

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.io.File
import kotlin.text.replace

object Utils {
    fun sendMessage(player: Player, message: String) {
        player.sendMessage("${ChatColor.GREEN}[Sheepy] ${Config.PRIMARY_COLOR}$message")
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

    fun getAnimsInFolder(plugin: Sheepy, folderName: String = ""): List<File>? {
        val pluginFolder = plugin.dataFolder
        val sanitizedFolderName = sanitizeString(folderName) ?: ""
        val animFolder = File(pluginFolder, sanitizedFolderName)

        val files = animFolder.listFiles()?.filter { file -> file.extension == Config.FILE_EXTENSION }

        return files?.toList()
    }

    fun sanitizeString(s: String?): String? {
        return s?.replace(Regex("[^a-zA-Z0-9_-]"), "")
    }
}