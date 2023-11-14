package org.example2.Sheepy;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AnimUtils {

    private static final List<List<AnimationParticle[]>> jumpAnims = new ArrayList<>();
    static class Animation {
        private boolean dontLoad = false;
        private final List<AnimationParticle[]> frames;

        private Animation(List<AnimationParticle[]> frames) {
            this.frames = frames;
        }
    }

    static class AnimationParticle {
        private final float[] position = new float[3];
        private Color color = Color.BLACK;
        private byte pscale = 0;
    }

    public enum ParticleType {
        REDSTONE,
        multiple_dust,
        SPELL_MOB,
        SPELL_MOB_AMBIENT,
        DUST_TRANSITION,
    }

    static final List<Animation> animations = new ArrayList<>();

    public static void loadAnimations() {

        InputStream[] jumpAnimFiles = {LinksuJump.getPlugin().getResource("anims/linksu1.bin"), LinksuJump.getPlugin().getResource("anims/linksu2.bin"), LinksuJump.getPlugin().getResource("anims/shny1.bin"), LinksuJump.getPlugin().getResource("anims/len1.bin")};

        for (InputStream animFile : jumpAnimFiles) {
//            LinksuJump.getPlugin().getLogger().info("loading: " + animFile);
            List<AnimationParticle[]> frames = new ArrayList<>();


            try {
                assert animFile != null;
                byte[] fileBytes = animFile.readAllBytes();
                ByteBuffer bb = ByteBuffer.wrap(fileBytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);

                while (bb.hasRemaining()) {
                    short length = bb.getShort();
                    AnimationParticle[] frame = new AnimationParticle[length];

                    // loop over particles and add them to frame
                    for (int i = 0; i < length; i++) {
                        frame[i] = new AnimationParticle();
                        for (int j = 0; j < 3; j++) {
                            short posComponent = bb.getShort();
                            float posComponentFloat = toFloat(posComponent);
                            frame[i].position[j] = posComponentFloat;
                        }
                        Color colorAndScale = Color.fromARGB(bb.getInt());
                        frame[i].color = colorAndScale;
                        frame[i].pscale = (byte) colorAndScale.getAlpha();
                    }
                    frames.add(frame);
                }
            } catch (Exception e) {
                Bukkit.getLogger().info(e.toString());
            }
            jumpAnims.add(frames);
        }
    }

    public static void playRandomFile(Player p) {
        // args: <filename> [particle scale] [animation scale] [particle type] [particle count per tick]


        // particle attrib: float/color/byte
        // particle:        AnimationParticle
        // frame:           AnimationParticle[]
        // animation:       Queue<AnimationParticle[]> or List<AnimationParticle[]>

        int idx = (int) (Math.random() * jumpAnims.size());
        List<AnimationParticle[]> frames = jumpAnims.get(idx);


        // particle spawner
        new BukkitRunnable() {

            private int counter = 0;

            @Override
            public void run() {
                Location loc = p.getLocation().add(0, 0.1, 0);
                AnimationParticle[] frame;
                try {
                    frame = frames.get(counter / JumpListener.frameRepeats);
                } catch (Exception e) {
                    this.cancel();
                    return;
                }
                if (frame == null) {
                    this.cancel();
                    return;
                }
                playFrame(frame, loc, ParticleType.REDSTONE, 2, 2);
                counter++;
            }

        }.runTaskTimer(LinksuJump.getPlugin(), 0L, 1L);

    }

    static void playFrame(AnimationParticle[] frame, Location loc, ParticleType particleType, float custom_pscale, float scale) {
        for (AnimationParticle p : frame) {

            World world = loc.getWorld();

            Color color = p.color;
            float pscale = Byte.toUnsignedInt(p.pscale);
            pscale = (pscale + 1) / 64;
            pscale *= custom_pscale;
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, pscale);

            float yaw = (float) ((loc.getYaw() + 180) * (Math.PI / 180));

            Vector pointPos = new Vector(p.position[0], p.position[1], p.position[2]).multiply(scale);

            pointPos = rotateY(pointPos, yaw);

            Vector pos = pointPos.add(loc.toVector());


            switch (particleType) {
                case REDSTONE:
                    world.spawnParticle(Particle.REDSTONE, pos.toLocation(world), 1, 0, 0, 0, 0, dustOptions, true);
                    break;

                case SPELL_MOB:
                    world.spawnParticle(Particle.SPELL_MOB, pos.toLocation(world), 0, 1 - color.getRed(), 1 - color.getGreen(), 1 - color.getBlue(), 1, null, true);
                    break;

                case SPELL_MOB_AMBIENT:
                    world.spawnParticle(Particle.SPELL_MOB_AMBIENT, pos.toLocation(world), 0, 1 - color.getRed(), 1 - color.getGreen(), 1 - color.getBlue(), 1, null, true);
                    break;

                case DUST_TRANSITION:
                    Particle.DustTransition dustTransition = new Particle.DustTransition(color, color, pscale);
                    world.spawnParticle(Particle.DUST_COLOR_TRANSITION, pos.toLocation(world), 0, dustTransition);
                    break;
            }


        }
    }


    // from https://stackoverflow.com/a/6162687
    // ignores the higher 16 bits
    public static float toFloat(int hbits) {
        int mant = hbits & 0x03ff;            // 10 bits mantissa
        int exp = hbits & 0x7c00;            // 5 bits exponent
        if (exp == 0x7c00)                   // NaN/Inf
            exp = 0x3fc00;                    // -> NaN/Inf
        else if (exp != 0)                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if (mant == 0 && exp > 0x1c400)  // smooth transition
                return Float.intBitsToFloat((hbits & 0x8000) << 16
                        | exp << 13 | 0x3ff);
        } else if (mant != 0)                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                   // mantissa * 2
                exp -= 0x400;                 // decrease exp by 1
            } while ((mant & 0x400) == 0); // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
                (hbits & 0x8000) << 16          // sign  << ( 31 - 15 )
                        | (exp | mant) << 13);         // value << ( 23 - 10 )
    }

    private static Vector rotateY(Vector vector, double angle) { // angle in radians

        float x1 = (float)(vector.getX() * Math.cos(angle) - vector.getZ() * Math.sin(angle));

        float z1 = (float)(vector.getX() * Math.sin(angle) + vector.getZ() * Math.cos(angle)) ;

        return new Vector(x1, vector.getY(), z1);

    }
}

