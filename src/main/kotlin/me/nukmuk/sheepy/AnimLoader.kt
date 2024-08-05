package me.nukmuk.sheepy

import org.bukkit.Bukkit
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class AnimLoader(private val plugin: Sheepy) {

    fun getAnimsInFolder(folderName: String = ""): List<File>? {
        val pluginFolder = plugin.dataFolder
        val sanitizedFolderName = folderName.replace(Regex("[^a-zA-Z0-9_-]"), "")
        val animFolder = File(pluginFolder, sanitizedFolderName)

        val files = animFolder.listFiles()?.filter { file -> file.extension == Config.FILE_EXTENSION }

        return files?.toList()
    }

    fun loadAnimFile(fileName: String): List<List<FloatArray>> {

        val pluginFolder = plugin.dataFolder
        val animFile = File(pluginFolder, fileName)

        // create list of frames
        plugin.logger.info("loading anim $fileName")
        val frames: MutableList<List<FloatArray>> = ArrayList()

        var particles: MutableList<FloatArray> = ArrayList()

        try {
            val br = BufferedReader(FileReader(animFile))
            var line: String

            //            int counter = 0;
            while ((br.readLine().also { line = it }) != null) {
                // add frame to frames

                if (line.startsWith("f")) {
                    frames.add(particles)
                    particles = ArrayList()

                    if (frames.size % 500 == 0) {
                        Bukkit.getLogger().info("loaded frame " + frames.size)
                    }

                    continue
                }

                //                counter++;
                val particle = convertToParticle(line)
                particles.add(particle)
            }
            br.close()
        } catch (e: Exception) {
            // send message to minecraft console
            plugin.logger.info("error reading file")
            //            plugin.getLogger().info(e.toString());
        }
        return frames
    }

    fun convertToParticle(line: String): FloatArray {
        val values = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val x = values[0].toFloat()
        val y = values[1].toFloat()
        val z = values[2].toFloat()
        val r = values[3].toFloat() * 255
        val g = values[4].toFloat() * 255
        val b = values[5].toFloat() * 255
        val s = if (values.size > 6) values[6].toFloat() else 1.0f

        return floatArrayOf(x, y, z, r, g, b, s)
    }

    fun loadFrame(file: File): List<FloatArray> {
        val particles: MutableList<FloatArray> = ArrayList()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String
            var counter = 0


            while ((br.readLine().also { line = it }) != null) {
                line = line.replace("(", "").replace(")", "").replace("\"", "")

                // don't read all lines
//                if (counter % 200 != 0) {
//                    counter++;
//                    continue;
//                }

                // skip reading first line
                if (counter == 0) {
                    counter++
                    continue
                }

                counter++
                val particle = convertToParticle(line)
                particles.add(particle)
            }
            br.close()
        } catch (e: Exception) {
            // send message to minecraft console
            plugin.logger.info("error reading csv file")
            plugin.logger.info(e.toString())
        }
        return particles
    }
}