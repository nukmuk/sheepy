package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import org.bukkit.Particle

object ParticleRenderer {
    fun playFrames(frames: List<Frame>, maxParticles: Int, plugin: Sheepy) {
        frames.forEach { playFrame(it, maxParticles, plugin) }
    }

    fun playFrame(frame: Frame, maxParticles: Int, plugin: Sheepy) {
        val total = frame.animationParticles.size
        val particleScale = frame.animation.particleScale

        val divider: Int = if (maxParticles == 0) 0 else total / maxParticles

        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)

        frame.animationParticles.forEachIndexed { idx, p ->
            if (p == null) return
            if (divider != 0 && idx % divider != 0) return@forEachIndexed
            frame.animation.world.spawnParticle(
                Particle.DUST,
                p.x.toDouble(),
                p.y.toDouble(),
                p.z.toDouble(),
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                Particle.DustOptions(p.color, p.scale.toFloat() / 255 * particleScale * scaleMultiplier * 5),
                plugin.config.getBoolean("increased-particle-render-distance")
            )
        }
    }
}