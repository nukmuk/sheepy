package me.nukmuk.sheepy

import org.bukkit.Color

data class Frame(val animationParticles: Array<AnimationParticle?>, val animation: Animation)

data class AnimationParticle(
    val x: Float,
    val y: Float,
    val z: Float,
    val color: Color
) {
    val scale: Byte
        get() = color.alpha.toByte()
}

enum class RenderType {
    PARTICLE,
    BLOCK_DISPLAY,
    TEXT_DISPLAY
}