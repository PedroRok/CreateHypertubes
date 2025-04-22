package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.commands.TestCommand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = EventBusSubscriber.Bus.MOD, modid = HypertubeMod.MOD_ID)
public class ModServerEvents {

    @SubscribeEvent
    static void onRegisterCommand(RegisterCommandsEvent event) {
        new TestCommand().register(event.getDispatcher());
    }
}
