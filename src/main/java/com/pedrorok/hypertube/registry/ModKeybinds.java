package com.pedrorok.hypertube.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = HypertubeMod.MOD_ID)
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