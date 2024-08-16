package me.nukmuk.sheepy.commands

import me.nukmuk.sheepy.Config
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.Utils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Abilities
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import kotlin.collections.getOrNull
import kotlin.text.toInt

class MiscCommand(private val plugin: Sheepy) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        s: String,
        args: Array<out String>?
    ): Boolean {
        val player = sender as Player

        if (!player.hasPermission("sheepy.misc")) {
            Utils.sendMessage(player, Config.Strings.NO_PERMISSION)
            return true
        }

        val craftPlayer = player as CraftPlayer
        val subcmd = args?.getOrNull(0)?.lowercase()

        if (subcmd == "1") {

            var i = 0
            plugin.server.scheduler.runTaskTimer(plugin, { task ->
                if (i > 20) task.cancel()
                i++
                for (otherPlayer in plugin.server.onlinePlayers) {
//                if (otherPlayer == player) continue
                    val otherCraftPlayer = (otherPlayer as CraftPlayer)
                    val connection = otherCraftPlayer.handle.connection
                    player as CraftPlayer
                    val value = if (i % 2 == 0) 4 else 5
                    repeat(1000) {
                        connection.send(ClientboundAnimatePacket(player.handle, value))
                    }
                }
            }, 0, 1)
        } else if (subcmd == "2") {
            for (otherPlayer in plugin.server.onlinePlayers) {
                (otherPlayer as CraftPlayer).handle.connection.sendPacket(
                    ClientboundEntityEventPacket(
                        otherPlayer.handle,
                        22.toByte()
                    )
                )
            }
        } else if (subcmd == "3") {
            for (otherPlayer in plugin.server.onlinePlayers) {
                (otherPlayer as CraftPlayer).handle.connection.sendPacket(
                    ClientboundEntityEventPacket(
                        otherPlayer.handle,
                        23.toByte()
                    )
                )
            }
        } else if (subcmd == "4") {
            var i = 0
            plugin.server.scheduler.runTaskTimer(plugin, { task ->
                if (i >= 20 * 5) task.cancel()
                for (otherPlayer in plugin.server.onlinePlayers) {
//                    if (otherPlayer != player) continue;
                    (otherPlayer as CraftPlayer).handle.connection.sendPacket(
                        ClientboundEntityEventPacket(
                            otherPlayer.handle,
                            35.toByte()
                        )
                    )
                }
                i++
            }, 0, 1)
        } else if (subcmd == "5") {
            for (otherPlayer in plugin.server.onlinePlayers) {
                val otherCraftPlayer = (otherPlayer as CraftPlayer)
                for (i in 1..1000) {
                    val level = (player.world as CraftWorld).handle
                    val entity = EntityType.BLOCK_DISPLAY.create(level)
                    otherCraftPlayer.handle.connection.sendPacket(
                        ClientboundAddEntityPacket(
                            entity!!.id,
                            entity.uuid,
                            otherPlayer.location.x + i * 0.1,
                            otherPlayer.location.y,
                            otherPlayer.location.z,
                            0f,
                            0f,
                            EntityType.PIG,
                            0,
                            Vec3(0.0, 0.0, 0.0),
                            0.0
                        )
                    )
                }
            }
//            player.sendMessage(entity.toString())
        } else if (subcmd == "6") {
            for (otherPlayer in plugin.server.onlinePlayers) {
                val otherCraftPlayer = otherPlayer as CraftPlayer
                otherCraftPlayer.handle.connection.sendPacket(ClientboundForgetLevelChunkPacket(ChunkPos(craftPlayer.handle.blockPosition())))
            }
        } else if (subcmd == "7") {
            val abilities = Abilities()
            abilities.walkingSpeed = args.getOrNull(1)?.toFloat()!!
            for (otherPlayer in plugin.server.onlinePlayers) {
                val otherCraftPlayer = otherPlayer as CraftPlayer
                otherCraftPlayer.handle.connection.sendPacket(ClientboundPlayerAbilitiesPacket(abilities))
            }
        } else if (subcmd == "8") {
            val msg = args.getOrNull(1)
            for (otherPlayer in plugin.server.onlinePlayers) {
                val otherCraftPlayer = otherPlayer as CraftPlayer
                otherCraftPlayer.handle.connection.sendPacket(
                    ClientboundPlayerCombatKillPacket(
                        otherCraftPlayer.entityId,
                        Component.nullToEmpty(msg)
                    )
                )
            }

        } else if (subcmd == "9") {
            val dist = args.getOrNull(1)?.toInt()
            dist?.let { player.viewDistance = it }
        } else if (subcmd == "10") {
            for (otherPlayer in plugin.server.onlinePlayers) {
                val otherCraftPlayer = otherPlayer as CraftPlayer
                otherCraftPlayer.handle.connection.send(
                    ClientboundGameEventPacket(
                        ClientboundGameEventPacket.Type(5),
                        0f
                    )
                )
            }
        }
        return true
    }
}