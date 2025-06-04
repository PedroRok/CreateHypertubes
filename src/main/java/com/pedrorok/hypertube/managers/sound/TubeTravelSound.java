package com.pedrorok.hypertube.managers.sound;

import lombok.Setter;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

@Setter
public class TubeTravelSound extends AbstractTickableSoundInstance {

	private float pitch;

	public TubeTravelSound(SoundEvent soundEvent, float pitch) {
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

}