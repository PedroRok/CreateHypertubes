package com.pedrorok.hypertube.ponder.scenes;

import com.jcraft.jorbis.Block;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. 29/01/2026
 * @project Create Hypertube
 */
public class EntranceScenes {

    public static void entranceScene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("entrance", "Creating a Hypertube");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.2f);
        scene.setSceneOffsetY(-1);

        // Show base
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos entrancePos = util.grid().at(2,1,2);

        Selection tubeS = util.select().fromTo(2,1,2,5,1,2);

        // cogs
        Selection sideCogS = util.select().position(2,1,3);

        setSystemSpeed(util, scene, 16);

        scene.world().setKineticSpeed(tubeS, 0);

        scene.world().showSection(tubeS, Direction.DOWN);
        scene.idle(20);
        scene.overlay()
                .showText(70)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().of(2, 1, 3.3))
                .placeNearTarget()
                .text("Hypertube Entrances connect to the Hypertube network, allowing high-speed travel.");

        scene.idle(60);

        for (int x = 5; x >= 2; x--) {
            scene.idle(2);
            scene.world().showSection(util.select().position(x, 1, 4), Direction.DOWN);
        }
        scene.idle(2);
        scene.world().setKineticSpeed(tubeS, 16);
        scene.world().showSection(sideCogS, Direction.EAST);
        changeOpenCloseEntrance(scene, entrancePos, true);
        scene.idle(20);
        scene.overlay()
                .showText(70)
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .pointAt(util.vector().of(2, 1, 3.3))
                .placeNearTarget()
                .text("To be able to enter the Hypertube, you need to power with at least 16 RPM.");
        scene.idle(20);

        // PARROT TRAVELLING
        ElementLink<ParrotElement> birb = scene.special()
                .createBirb(new Vec3(1, 1, 2.5), ParrotPose.DancePose::new);

        scene.idle(40);
        scene.special().moveParrot(birb, new Vec3(0, 0.1, 0), 5);
        scene.special().changeBirbPose(birb, ParrotPose.FlappyPose::new);
        scene.idle(3);
        scene.special().moveParrot(birb, new Vec3(5.5, 0, 0), 20);
        scene.idle(5);
        scene.special().hideElement(birb, Direction.EAST);

        scene.idle(30);
        scene.overlay()
                .showText(70)
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .pointAt(util.vector().of(2, 1, 3.3))
                .placeNearTarget()
                .text("The faster it spins, the faster you can travel.");
        scene.world().multiplyKineticSpeed(util.select().everywhere(), 4);
        scene.effects().rotationSpeedIndicator(util.grid().at(5, 1, 4));
        scene.idle(30);

        scene.idle(80);
        setSystemSpeed(util, scene, 32);
    }

    private static void setSystemSpeed(SceneBuildingUtil util, CreateSceneBuilder scene, int entranceSpeed) {
        Selection mainShaftS = util.select().fromTo(2,1,5,5,1,4);
        Selection sideCogS = util.select().position(2,1,3);
        Selection baseCog = util.select().position(5,0,3);
        Selection tubeS = util.select().position(2,1,2);


        scene.world().setKineticSpeed(baseCog, -entranceSpeed * 2);
        scene.world().setKineticSpeed(mainShaftS, entranceSpeed);
        scene.world().setKineticSpeed(sideCogS, -entranceSpeed);
        scene.world().setKineticSpeed(tubeS, entranceSpeed);
    }

    public static void changeOpenCloseEntrance(SceneBuilder builder, BlockPos p1, boolean open) {
        builder.world()
                .modifyBlock(p1, blockState -> blockState.setValue(HyperEntranceBlock.OPEN, open), false);
    }
}
