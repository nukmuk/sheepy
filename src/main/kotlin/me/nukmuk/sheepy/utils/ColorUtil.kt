package me.nukmuk.sheepy.utils

import me.nukmuk.sheepy.Sheepy
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import kotlin.math.sqrt

object ColorUtil {
    data class ColorBlock(val block: Block, val color: Color)

    val uniqueBlockColors: List<ColorBlock> by lazy { generateUniqueBlockColors() }

    private fun generateUniqueBlockColors(): List<ColorBlock> {
        val colorMap = mutableMapOf<Int, ColorBlock>()

        for (block in BuiltInRegistries.BLOCK) {
            val materialColor = block.defaultMapColor()
            val color = Color.fromRGB(materialColor.col)
            val colorKey = color.asRGB()

            if (colorMap.containsKey(colorKey)) continue
            if (block.toString().contains("minecraft:air")) {
                colorMap[colorKey] = ColorBlock(Blocks.BLACK_CONCRETE, color)
                continue
            }
//            if (block.toString().contains("minecraft:glass")) continue
            if (!block.toString().contains("concrete") && !block.toString().contains("wool") && !block.toString()
                    .contains("plank") && !block.toString().contains("stone") && !block.toString()
                    .contains("terracotta")
            ) continue
//            if (block.toString().contains("rail")) continue
//            if (block.toString().contains("torch")) continue
//            if (block.toString().contains("redstone")) continue
            if (block.toString().contains("cobweb")) continue
            colorMap[colorKey] = ColorBlock(block, color)
        }
        Bukkit.getLogger().info("generated ${colorMap.size} colors: ${colorMap.values.joinToString(", ")}")
        return colorMap.values.toList()
    }

    fun getBlockWithColor(targetColor: Color): Block {
        return findClosestBlockColor(targetColor).block
    }

    fun findClosestBlockColor(targetColor: Color): ColorBlock {
        return uniqueBlockColors.minByOrNull { calculateColorDistance(it.color, targetColor) }!!
    }

    private fun calculateColorDistance(color1: Color, color2: Color): Double {
        val rDiff = color1.red - color2.red
        val gDiff = color1.green - color2.green
        val bDiff = color1.blue - color2.blue
        return sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
    }

    fun getColor(material: Material, plugin: Sheepy): Color {
        val block: Block = CraftMagicNumbers.getBlock(material)
        val mapColor = block.defaultMapColor()
        plugin.logger.info("Color: $mapColor")
        val blockName = "diamond_block"
        val block2 = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(blockName))
        return Color.fromRGB(mapColor.col)
    }
}