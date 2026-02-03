package com.pedrorok.hypertube.ponder.scenes;

import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
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
public class AcceleratorScenes {

    public static void acceleratorScene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("accelerator", "Hypertube Accelerator");
        scene.configureBasePlate(1, 0, 5);
        scene.scaleSceneView(1.2f);
        scene.setSceneOffsetY(0);

        // Show base
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos entrancePos = util.grid().at(3,1,2);

        Selection tubeS = util.select().fromTo(0,1,2,6,1,2);

        // cogs
        Selection sideCogS = util.select().position(3,1,3);

        scene.world().setKineticSpeed(tubeS, 0);

        scene.world().showSection(tubeS, Direction.DOWN);
        scene.idle(20);

        for (int x = 6; x >= 3; x--) {
            scene.idle(2);
            scene.world().showSection(util.select().position(x, 1, 4), Direction.DOWN);
        }
        scene.idle(2);
        scene.world().setKineticSpeed(tubeS, 64);
        scene.world().showSection(sideCogS, Direction.EAST);
        changeAccelerateMode(scene, entrancePos, true);
        scene.idle(20);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().of(3, 1.5, 2.5))
                .placeNearTarget()
                .text("The Hyper Accelerator increases the speed of entities traveling the Hypertube.");
        scene.idle(80);

        // PARROT TRAVELLING
        ElementLink<ParrotElement> birb = scene.special()
                .createBirb(new Vec3(0, 1.1, 2.5), ParrotPose.FlappyPose::new);
        scene.special().moveParrot(birb, new Vec3(3, 0, 0), 30);
        scene.idle(20);
        changeOpenCloseEntrance(scene, entrancePos, true);
        scene.idle(10);
        scene.special().moveParrot(birb, new Vec3(10, 0, 0), 20);
        scene.idle(10);
        changeOpenCloseEntrance(scene, entrancePos, false);
        scene.special().hideElement(birb, Direction.EAST);

        scene.idle(20);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().of(3, 1.5, 2.5))
                .placeNearTarget()
                .text("You can change to BRAKE mode by Right Clicking the Accelerator.");
        scene.idle(10);
        scene.overlay()
                .showControls(entrancePos.getCenter().add(0,1,0), Pointing.DOWN, 40)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        changeAccelerateMode(scene, entrancePos, false);
        scene.effects().indicateSuccess(entrancePos);
        scene.idle(80);

        // PARROT TRAVELLING
        ElementLink<ParrotElement> birb2 = scene.special()
                .createBirb(new Vec3(0, 1.1, 2.5), ParrotPose.FlappyPose::new);
        scene.special().moveParrot(birb2, new Vec3(3, 0, 0), 15);
        scene.idle(10);
        changeOpenCloseEntrance(scene, entrancePos, true);
        scene.idle(5);
        scene.special().moveParrot(birb2, new Vec3(10, 0, 0), 80);
        scene.idle(30);
        changeOpenCloseEntrance(scene, entrancePos, false);
        scene.idle(40);
        scene.special().hideElement(birb2, Direction.EAST);
    }

    private static void setSystemSpeed(SceneBuildingUtil util, CreateSceneBuilder scene, int entranceSpeed) {
        Selection mainShaftS = util.select().fromTo(3,1,5,6,1,4);
        Selection sideCogS = util.select().position(3,1,3);
        Selection baseCog = util.select().position(6,0,3);
        Selection tubeS = util.select().position(3,1,2);


        scene.world().setKineticSpeed(baseCog, -entranceSpeed * 2);
        scene.world().setKineticSpeed(mainShaftS, entranceSpeed);
        scene.world().setKineticSpeed(sideCogS, -entranceSpeed);
        scene.world().setKineticSpeed(tubeS, entranceSpeed);
    }

    public static void changeAccelerateMode(SceneBuilder builder, BlockPos p1, boolean accelerate) {
        builder.world()
                .modifyBlock(p1, blockState -> blockState.setValue(HyperAcceleratorBlock.ACCELERATE, accelerate), false);
    }

    public static void changeOpenCloseEntrance(SceneBuilder builder, BlockPos p1, boolean open) {
        builder.world()
                .modifyBlock(p1, blockState -> blockState.setValue(HyperAcceleratorBlock.OPEN, open), false);
    }
}
