package me.nukmuk.sheepy

import org.bukkit.Color

data class Frame(val animationParticles: Array<AnimationParticle?>, val animation: Animation)

data class AnimationParticle(
    val x: Float,
    val y: Float,
    val z: Float,
    val color: Color,
    val frame: Frame
) {
    val scale: Byte
        get() = color.alpha.toByte()
}

enum class RenderType {
    PARTICLE,
    BLOCK_DISPLAY_PACKET,
    TEXT_DISPLAY_PACKET,
    TEXT_DISPLAY
}

fun parseRenderType(string: String?): RenderType? = RenderType.entries.find { it.name == string }

enum class TextMode {
    BACKGROUND,
    TEXT
}

fun parseTextMode(string: String?): TextMode? = TextMode.entries.find { it.name == string }


enum class RandomRotation {
    NONE,
    YAW,
    FULL
}

fun parseRandomRotation(string: String?): RandomRotation? = RandomRotation.entries.find { it.name == string }
// todo generic enum parser

enum class ShouldBeLeftInWorld {
    NO,
    YES,
    YES_AND_MAKE_PERSISTENT
}