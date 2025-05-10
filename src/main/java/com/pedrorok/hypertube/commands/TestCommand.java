package com.pedrorok.hypertube.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pedrorok.hypertube.managers.TravelManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class TestCommand implements CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("remove-bug")
                .executes(this::execute);
    }


    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ctx.getSource().isPlayer()) {
            ctx.getSource().sendSystemMessage(Component.literal("Only player can use this command"));
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TravelManager.removePlayerFromTravel(player);
        player.sendSystemMessage(Component.literal("Removed from travel"));
        return 1;
    }
}