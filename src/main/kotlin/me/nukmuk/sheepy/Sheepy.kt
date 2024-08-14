package me.nukmuk.sheepy

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin


class Sheepy : JavaPlugin() {

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).usePluginNamespace())
    }

    override fun onEnable() {
        // Plugin startup logic
        CommandAPI.onEnable()
        getCommand("sheepy")?.setExecutor(SheepyCommand(this))
        getCommand("misc")?.setExecutor(MiscCommand(this))
        AnimationsPlayer.initialize(this)

        commandTree("optionalArgument") {
            literalArgument("give") {
                itemStackArgument("item") {
                    integerArgument("amount", optional = true) {
                        playerExecutor { player, args ->
                            // This command will let you execute:
                            // "/optionalArgument give minecraft:stick"
                            // "/optionalArgument give minecraft:stick 5"
                            val itemStack: ItemStack = args["item"] as ItemStack
                            val amount: Int = args.getOptional("amount").orElse(1) as Int
                            itemStack.amount = amount
                            player.inventory.addItem(itemStack)
                        }
                    }
                }
            }
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
        CommandAPI.onDisable()
    }
}
