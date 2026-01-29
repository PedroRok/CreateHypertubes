package com.pedrorok.hypertube.ponder.scenes;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/**
 * @author Rok, Pedro Lucas nmm. 27/01/2026
 * @project Create Hypertube
 */
public class TubeScenes {

    public static void simpleTube(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("simple_tube_test", "Testing Hypertube Rendering");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.2f);
        scene.setSceneOffsetY(0);
        // Show base
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(5);


        BlockPos block1 = util.grid()
                .at(3, 1, 1);

        Vec3 c1 = util.vector()
                .centerOf(block1);
        AABB bb1 = new AABB(c1, c1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, block1, bb1, 10);
        scene.idle(1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, block1, bb1.inflate(0.25, 0.5, 0.5), 100);
        scene.idle(10);
    }


}
