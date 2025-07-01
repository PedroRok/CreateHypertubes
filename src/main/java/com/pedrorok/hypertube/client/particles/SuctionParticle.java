package com.pedrorok.hypertube.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 01/07/2025
 * @project Create Hypertube
 */
public class SuctionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final Vec3 initialVelocity;
    private final float ACCELERATION_FACTOR = 5.0f;

    public SuctionParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz,
                           SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);

        this.gravity = 0f;
        this.hasPhysics = false;

        this.lifetime = 15;
        this.alpha = 0f;

        this.initialVelocity = new Vec3(vx, vy, vz);

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.roll = 10;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);

        float progress = (float) this.age / this.lifetime;
        float velocityMultiplier = ACCELERATION_FACTOR * (progress * progress);

        Vec3 newVelocity = initialVelocity.scale(velocityMultiplier);
        this.x += newVelocity.x;
        this.y += newVelocity.y;
        this.z += newVelocity.z;

        if (progress < 0.5)
            this.alpha = progress;
        else
            this.alpha = 0.5f - progress;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SuctionParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
