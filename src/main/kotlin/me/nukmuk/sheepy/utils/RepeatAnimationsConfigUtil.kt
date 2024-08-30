package me.nukmuk.sheepy.utils

import me.nukmuk.sheepy.Animation
import me.nukmuk.sheepy.AnimationsManager
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.l
import me.nukmuk.sheepy.parseRandomRotation
import me.nukmuk.sheepy.parseRenderType
import me.nukmuk.sheepy.parseTextMode
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object RepeatAnimationsConfigUtil {
    private val plugin = Sheepy.instance
    private val file = File(plugin.dataFolder, "repeat-animations.yml")
    private val config = YamlConfiguration.loadConfiguration(file)

    fun set(path: String, value: Any?) {
        config.set(path, value)
    }

    fun save() {
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            config.save(file)
        })
    }

    fun updateValueIfRepeating(path: String, value: Any?) {
        set(path, value)
        save()
    }

    fun saveAnimation(animation: Animation) {
        set("${animation.name}.file", animation.file.name)
        set("${animation.name}.location", animation.location)
        set("${animation.name}.particlescale", animation.particleScale)
        set("${animation.name}.animationscale", animation.animationScale)
        set("${animation.name}.rotationY", animation.animationRotationY)
        set("${animation.name}.rotationX", animation.animationRotationX)
        set("${animation.name}.rotationZ", animation.animationRotationZ)
        set("${animation.name}.rendertype", animation.renderType.toString())
        set("${animation.name}.playing", true)
        set("${animation.name}.textForTextRenderer", animation.textForTextRenderer)
        set("${animation.name}.textMode", animation.textMode.toString())
        set("${animation.name}.randomRotationMode", animation.randomRotationMode.toString())
        save()
    }


    fun loadAnimation(name: String) {
        try {
            val fileName = config.getString("${name}.file")
            if (fileName == null) {
                plugin.logger.warning("animation $name fileName null")
                return
            }
            val file = File(plugin.dataFolder, fileName)
            val location = config.getLocation("${name}.location")
            val pscale = config.getDouble("${name}.particlescale")
            val ascale = config.getDouble("${name}.animationscale")
            val rotY = config.getDouble("${name}.rotationY")
            val rotX = config.getDouble("${name}.rotationX")
            val rotZ = config.getDouble("${name}.rotationZ")
            val rt = config.getString("${name}.rendertype")
            val p = config.getBoolean("${name}.playing")
            val t = config.getString("${name}.textForTextRenderer")
            val tm = config.getString("${name}.textMode")
            val rtm = config.getString("${name}.randomRotationMode")

            val animation = AnimationsManager.createAnimation(name, file, location!!, true)
            animation.particleScale = pscale.toFloat()
            animation.animationScale = ascale.toFloat()
            animation.animationRotationY = rotY.toFloat()
            animation.animationRotationX = rotX.toFloat()
            animation.animationRotationZ = rotZ.toFloat()
            animation.renderType = parseRenderType(rt!!)
            animation.playing = p
            animation.textForTextRenderer = t!!
            animation.textMode = parseTextMode(tm)!!
            animation.randomRotationMode = parseRandomRotation(rtm)!!
        } catch (e: Exception) {
            plugin.logger.warning("Loading animation $name failed: $e")
        }
    }

    fun loadAllAnimations() {
        l("starting load")
        val names = config.getKeys(false)
        plugin.logger.info("Loading animations: $names")
        names.forEach { loadAnimation(it) }
    }

    fun remove(name: String) {
        config.set(name, null)
        save()
    }
}