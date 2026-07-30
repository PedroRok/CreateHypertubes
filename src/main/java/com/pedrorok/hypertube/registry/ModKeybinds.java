package com.pedrorok.hypertube.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = HypertubeMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum ModKeybinds {

	ESCAPE("tube_escape", GLFW.GLFW_KEY_LEFT_SHIFT);

	public static final String CATEGORY = "key.categories.hypertube";

	private final KeyMapping mapping;

	ModKeybinds(String description, int defaultKey) {
		this.mapping = new KeyMapping("key.hypertube." + description, defaultKey, CATEGORY);;
	}

	public boolean isDown() {
		return !this.mapping.isUnbound() && isKeyPressed();
	}

	private boolean isKeyPressed() {
		int keyCode = mapping.getKey().getValue();
		long window = Minecraft.getInstance().getWindow().getWindow();
		return InputConstants.isKeyDown(window, keyCode) && mapping.isConflictContextAndModifierActive();
	}

	public Component message() {
		return this.mapping.getTranslatedKeyMessage();
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		for (ModKeybinds key : values()) {
			event.register(key.mapping);
		}
	}
}