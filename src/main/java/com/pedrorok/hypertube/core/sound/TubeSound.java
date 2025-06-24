package com.pedrorok.hypertube.core.sound;

import lombok.Setter;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

@Setter
public class TubeSound extends AbstractTickableSoundInstance {

	private float pitch;

	public TubeSound(SoundEvent soundEvent, float pitch) {
		super(soundEvent, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
		this.pitch = pitch;
		volume = 0.01f;
		looping = true;
		delay = 0;
		relative = true;
	}

	@Override
	public void tick() {}

    public void fadeIn(float maxVolume) {
		volume = Math.min(maxVolume, volume + .05f);
	}

	public void fadeOut() {
		volume = Math.max(0, volume - 0.05f);
	}

	public boolean isFaded() {
		return volume == 0;
	}

	@Override
	public float getPitch() {
		return pitch;
	}

	public void stopSound() {
		stop();
	}

	public void updateLocation(Vec3 pos) {
		x = pos.x;
		y = pos.y;
		z = pos.z;
	}

	@Override
	public boolean isRelative() {
		return true;
	}
}