package com.pedrorok.hypertube.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public interface CommandBase {
	default void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(getBaseCommandBuilder());
	}

	LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder();
}