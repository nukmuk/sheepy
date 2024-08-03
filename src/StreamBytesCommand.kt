package me.nukmuk.sheepy

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.util.concurrent.BlockingQueue
import java.util.function.Consumer

class StreamBytesCommand : CommandExecutor {
    class Animation(frames: BlockingQueue<Array<AnimationParticle>>) {
        var dontLoad: Boolean = false
        val frames: BlockingQueue<Array<AnimationParticle>> = frames
    }

    class AnimationParticle {
        val position: FloatArray = FloatArray(3)
        var color: Color = Color.BLACK
        var pscale: Byte = 0
    }

    enum class ParticleType {
        REDSTONE,
        multiple_dust,
        SPELL_MOB,
        SPELL_MOB_AMBIENT,
        DUST_TRANSITION,
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        // args: <filename> [particle scale] [animation scale] [particle type] [particle count per tick]

        if (sender !is Player && sender !is BlockCommandSender) {
            sender.sendMessage(ChatColor.RED.toString() + "only players can use this command - " + sender.javaClass)
            //            return true;
        }

        if (args.size == 0) {
            sender.sendMessage("please specify file")
            return true
        }

        stopParticleTasks()

        val fileName = args[0].replace("[^a-zA-Z0-9]".toRegex(), "") + ".shny"
        //        ParticleType particleType = args.length > 3 ? ParticleType.valueOf(args[3]) : ParticleType.REDSTONE;
        val particleType = if (args.size > 3) when (args[3]) {
            "REDSTONE" -> ParticleType.REDSTONE
            "SPELL_MOB" -> ParticleType.SPELL_MOB
            "SPELL_MOB_AMBIENT" -> ParticleType.SPELL_MOB_AMBIENT
            "DUST_TRANSITION" -> ParticleType.DUST_TRANSITION
            "multiple_dust" -> ParticleType.multiple_dust
            else -> null
        } else ParticleType.REDSTONE

        if (particleType == null) {
            sender.sendMessage(ChatColor.RED.toString() + "invalid particle type: " + args[3])
            return true
        }


        // particle attrib: float/color/byte
        // particle:        AnimationParticle
        // frame:           AnimationParticle[]
        // animation:       Queue<AnimationParticle[]>
        val frames: BlockingQueue<Array<AnimationParticle>> =
            ArrayBlockingQueue<Array<AnimationParticle>>(1) // smaller numbers seem to have better performance
        val loc = getLocation(sender)
        val animation = Animation(frames)
        animations.add(animation)

        // particle spawner
        object : BukkitRunnable() {
            override fun run() {
                sender.sendActionBar(Component.text(ChatColor.GRAY.toString() + "frames in queue: " + frames.size))
                val frame: Array<AnimationParticle> = frames.poll()
                if (frame == null) {
                    if (animation.dontLoad) {
                        sender.sendActionBar(Component.text(ChatColor.GREEN.toString() + "end :)"))
                        animation.frames.clear()
                        animations.remove(animation)
                        this.cancel()
                        return
                    }
                    sender.sendActionBar(Component.text(ChatColor.RED.toString() + "lagaa"))
                    return
                }
                playFrame(frame, args, loc!!, particleType)
            }
        }.runTaskTimer(plugin, 0L, 1L)

        // file reader
        object : BukkitRunnable() {
            override fun run() {
                val pluginFolder: File = plugin.getDataFolder()
                val animFile = File(pluginFolder, fileName)

                plugin.getLogger().info("streaming: $fileName")

                try {
                    val fileBytes = Files.readAllBytes(animFile.toPath())
                    val bb = ByteBuffer.wrap(fileBytes)
                    bb.order(ByteOrder.LITTLE_ENDIAN)

                    while (bb.hasRemaining() && !animation.dontLoad) {
                        val length = bb.getShort()
                        val frame = arrayOfNulls<AnimationParticle>(length.toInt())

                        // loop over particles and add them to frame
                        for (i in 0 until length) {
                            frame[i] = AnimationParticle()
                            for (j in 0..2) {
                                val posComponent = bb.getShort()
                                val posComponentFloat = toFloat(posComponent.toInt())
                                frame[i]!!.position[j] = posComponentFloat
                            }
                            val colorAndScale = Color.fromARGB(bb.getInt())
                            frame[i]!!.color = colorAndScale
                            frame[i]!!.pscale = colorAndScale.alpha.toByte()
                        }
                        frames.put(frame)
                    }
                    animation.dontLoad = true
                } catch (e: Exception) {
//                    Bukkit.getLogger().info(e.toString());
                    animation.dontLoad = true
                    sender.sendMessage(ChatColor.RED.toString() + "error streaming file: " + e)
                }
            }
        }.runTaskAsynchronously(plugin)

        return true
    }

    companion object {
        val animations: MutableList<Animation> = ArrayList()

        @JvmOverloads
        fun playFrame(
            frame: Array<AnimationParticle>,
            args: Array<String>,
            loc: Location,
            particleType: ParticleType = ParticleType.REDSTONE
        ) {
            for (p in frame) {
                val world: World = loc.world

                val color = p.color
                var pscale_multiplier = 1f
                try {
                    if (args.size > 1) pscale_multiplier = args[1].toFloat()
                } catch (ignored: Exception) {
                }
                var pscale = java.lang.Byte.toUnsignedInt(p.pscale).toFloat()
                pscale = (pscale + 1) / 64
                pscale *= pscale_multiplier
                val dustOptions: DustOptions = DustOptions(color, pscale)

                var scale = 1f

                try {
                    if (args.size > 2) scale = args[2].toFloat()
                } catch (ignored: Exception) {
                }

                val pointPos = Vector(p.position[0], p.position[1], p.position[2]).multiply(scale)
                val pos = pointPos.add(loc.toVector())

                when (particleType) {
                    ParticleType.REDSTONE -> world.spawnParticle<DustOptions>(
                        Particle.REDSTONE,
                        pos.toLocation(world),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        dustOptions,
                        true
                    )

                    ParticleType.multiple_dust -> {
                        // spawn ~16k particles total every tick so previous frames disappear
                        var particlesPerTick = 8000
                        try {
                            if (args.size > 4) particlesPerTick = args[4].toInt()
                        } catch (ignored: Exception) {
                        }
                        val amount = Math.floorDiv(particlesPerTick, frame.size)
                        world.spawnParticle<DustOptions>(
                            Particle.REDSTONE,
                            pos.toLocation(world),
                            amount,
                            0.0,
                            0.0,
                            0.0,
                            1.0,
                            dustOptions,
                            true
                        )
                    }

                    ParticleType.SPELL_MOB -> world.spawnParticle<Any>(
                        Particle.SPELL_MOB,
                        pos.toLocation(world),
                        0,
                        (1 - color.red).toDouble(),
                        (1 - color.green).toDouble(),
                        (1 - color.blue).toDouble(),
                        1.0,
                        null,
                        true
                    )

                    ParticleType.SPELL_MOB_AMBIENT -> world.spawnParticle<Any>(
                        Particle.SPELL_MOB_AMBIENT,
                        pos.toLocation(world),
                        0,
                        (1 - color.red).toDouble(),
                        (1 - color.green).toDouble(),
                        (1 - color.blue).toDouble(),
                        1.0,
                        null,
                        true
                    )

                    ParticleType.DUST_TRANSITION -> {
                        val dustTransition: DustTransition = DustTransition(color, color, pscale)
                        world.spawnParticle<DustTransition>(
                            Particle.DUST_COLOR_TRANSITION,
                            pos.toLocation(world),
                            0,
                            dustTransition
                        )
                    }
                }
            }
        }

        fun getLocation(sender: CommandSender): Location? {
            val offset = Vector(0.5, 1.0, 0.5)
            if (sender is Player) {
                return sender.getTargetBlock(null, 64).getLocation().add(offset)
            } else if (sender is BlockCommandSender) {
                return sender.getBlock().getLocation().add(offset)
            }
            return null
        }

        fun stopParticleTasks() {
            animations.forEach(Consumer { animation: Animation ->
                animation.dontLoad = true
                animation.frames.clear()
            })
            animations.clear()
        }

        // from https://stackoverflow.com/a/6162687
        // ignores the higher 16 bits
        fun toFloat(hbits: Int): Float {
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
    }
}
