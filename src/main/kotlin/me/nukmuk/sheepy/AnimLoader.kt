package me.nukmuk.sheepy

import java.io.File

class AnimLoader(private val plugin: Sheepy) {

    fun getAnimsInFolder(folderName: String = ""): List<File>? {
        val pluginFolder = plugin.dataFolder
        val sanitizedFolderName = Utils.sanitizeString(folderName) ?: ""
        val animFolder = File(pluginFolder, sanitizedFolderName)

        val files = animFolder.listFiles()?.filter { file -> file.extension == Config.FILE_EXTENSION }

        return files?.toList()
    }
}