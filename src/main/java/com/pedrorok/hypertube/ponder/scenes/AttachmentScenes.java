package com.pedrorok.hypertube.ponder.scenes;

import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.pedrorok.hypertube.items.TubeAttachmentItem;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. 29/01/2026
 * @project Create Hypertube
 */
public class AttachmentScenes {

    public static void attachmentScene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final TubeAttachmentItem tubeScannerItem = ModItems.TUBE_SCANNER.get();
        final TubeAttachmentItem redstoneDetectorItem = ModItems.REDSTONE_DETECTOR.get();

        scene.title("attachments", "Hypertube Attachments");
        scene.configureBasePlate(1, 0, 5);
        scene.scaleSceneView(1.2f);
        scene.setSceneOffsetY(0);

        // Show base
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos acceleratorPos = util.grid().at(3, 3, 2);
        BlockPos entrancePos = util.grid().at(3, 1, 2);
        BlockPos redstonePos = util.grid().at(3, 1, 1);
        BlockPos redstoneLampPos = util.grid().at(3, 1, 0);

        Selection tubeS = util.select().fromTo(0, 3, 2, 6, 3, 2);
        Selection entranceTubeS = util.select().fromTo(3, 1, 2, 6, 1, 2);
        Selection redstoneLampS = util.select().position(redstoneLampPos);
        Selection redstoneCircuitS = util.select().position(redstonePos);


        // cogs
        Selection sideCogS = util.select().position(3, 1, 3);

        scene.world().setKineticSpeed(tubeS, 0);
        scene.world().showSection(entranceTubeS, Direction.DOWN);
        scene.idle(5);

        for (int x = 6; x >= 3; x--) {
            scene.idle(2);
            scene.world().showSection(util.select().position(x, 1, 4), Direction.DOWN);
        }
        scene.idle(2);
        scene.world().setKineticSpeed(tubeS, 64);
        scene.world().showSection(sideCogS, Direction.EAST);
        scene.world().setKineticSpeed(entranceTubeS, 32);
        scene.idle(20);
        scene.overlay().showControls(util.vector().of(4.2, 1., 2.5), Pointing.RIGHT, 40)
                .rightClick()
                .withItem(tubeScannerItem.getDefaultInstance());
        scene.idle(5);
        changeAttachment(builder, entrancePos, Direction.NORTH, tubeScannerItem.getTubeAttachment());
        scene.idle(5);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().of(3.5, 1.0, 2.5))
                .placeNearTarget()
                .text("The Scanner Attachment allows you to detect entities traveling the Hypertube.");
        scene.idle(60);

        // REDSTONE THING
        scene.world().modifyBlock(redstonePos, state -> {
            return state.setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE)
                    .setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE)
                    .setValue(RedStoneWireBlock.EAST, RedstoneSide.NONE)
                    .setValue(RedStoneWireBlock.WEST, RedstoneSide.NONE);
        }, false);
        scene.idle(5);
        scene.world().showSection(redstoneCircuitS, Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(redstoneLampS, Direction.SOUTH);

        scene.idle(30);
        // PARROT TRAVELLING
        changeOpenCloseEntrance(scene, entrancePos, true);
        ElementLink<ParrotElement> birb = scene.special()
                .createBirb(new Vec3(1.5, 1, 2.5), ParrotPose.DancePose::new);

        scene.idle(40);
        scene.special().moveParrot(birb, new Vec3(0, 0.1, 0), 5);
        scene.special().changeBirbPose(birb, ParrotPose.FlappyPose::new);
        scene.idle(3);

        scene.special().moveParrot(birb, new Vec3(5.5, 0, 0), 20);
        scene.idle(4);
        powerEverything(util, scene, true, false);
        scene.idle(4);
        changeOpenCloseEntrance(scene, entrancePos, false);
        scene.special().hideElement(birb, Direction.EAST);
        scene.idle(20);
        powerEverything(util, scene, false, false);
        scene.idle(20);

        scene.world().hideSection(entranceTubeS, Direction.UP);
        scene.idle(20);
        scene.world().modifyBlock(acceleratorPos, blockState -> blockState.setValue(HyperAcceleratorBlock.ACTIVE, true), false);
        scene.world().setKineticSpeed(tubeS, 32);
        changeAttachment(builder, acceleratorPos, Direction.NORTH, tubeScannerItem.getTubeAttachment());
        ElementLink<WorldSectionElement> tubeElement =
                scene.world().showIndependentSection(tubeS, Direction.DOWN);
        scene.world().moveSection(tubeElement, new Vec3(0, -2, 0), 0);

        scene.idle(10);
        scene.overlay()
                .showText(40)
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().of(3.5, 1.0, 2.5))
                .placeNearTarget()
                .text("You can place in accelerators too, in any side at the same time");
        scene.idle(40);

        // PARROT TRAVELLING
        ElementLink<ParrotElement> birb2 = scene.special()
                .createBirb(new Vec3(0, 1.1, 2.5), ParrotPose.FlappyPose::new);
        scene.special().moveParrot(birb2, new Vec3(3, 0, 0), 30);
        scene.idle(20);
        changeOpenCloseEntrance(scene, acceleratorPos, true);
        scene.idle(10);
        powerEverything(util, scene, true, false);
        scene.special().moveParrot(birb2, new Vec3(10, 0, 0), 40);
        scene.idle(10);
        changeOpenCloseEntrance(scene, acceleratorPos, false);
        scene.special().hideElement(birb2, Direction.EAST);
        scene.idle(20);
        powerEverything(util, scene, false, false);
        scene.idle(20);

        // Change to redstone
        scene.world().hideSection(redstoneLampS, Direction.UP);
        scene.idle(12);
        scene.world().setBlock(redstoneLampPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
        scene.world().showSection(redstoneLampS, Direction.DOWN);
        scene.idle(12);
        scene.overlay().showControls(Vec3.atCenterOf(entrancePos).add(0.8, -0.5, 0), Pointing.RIGHT, 20)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(12);
        removeAttachment(scene, acceleratorPos, Direction.NORTH);
        scene.idle(20);


        scene.overlay().showControls(Vec3.atCenterOf(entrancePos).add(0.8, -0.5, 0), Pointing.RIGHT, 60)
                .rightClick()
                .withItem(redstoneDetectorItem.getDefaultInstance());
        scene.idle(5);
        changeAttachment(builder, acceleratorPos, Direction.NORTH, redstoneDetectorItem.getTubeAttachment());
        scene.overlay()
                .showText(80)
                .attachKeyFrame()
                .pointAt(util.vector().of(3.5, 1.0, 2.5))
                .placeNearTarget()
                .text("You can change the Accelerator/Entrance mode with a Redstone signal.");

        scene.idle(60);
        scene.overlay().showControls(Vec3.atCenterOf(redstoneLampPos).add(0.2, -0.1, 0), Pointing.RIGHT, 20).rightClick();
        scene.idle(5);
        scene.world().modifyBlock(redstoneLampPos, state -> {
            return state.setValue(LeverBlock.POWERED, true);
        }, false);
        scene.effects().indicateSuccess(redstoneLampPos);
        powerEverything(util, scene, true, true);
        scene.world().modifyBlock(acceleratorPos, blockState -> blockState.setValue(HyperAcceleratorBlock.ACCELERATE, false), false);
        scene.idle(40);

        // final bird
        ElementLink<ParrotElement> birb3 = scene.special()
                .createBirb(new Vec3(0, 1.1, 2.5), ParrotPose.FlappyPose::new);
        scene.special().moveParrot(birb3, new Vec3(3, 0, 0), 15);
        scene.idle(10);
        changeOpenCloseEntrance(scene, acceleratorPos, true);
        scene.idle(5);
        scene.special().moveParrot(birb3, new Vec3(10, 0, 0), 80);
        scene.idle(30);
        changeOpenCloseEntrance(scene, acceleratorPos, false);
        scene.idle(40);
        scene.special().hideElement(birb3, Direction.EAST);
    }

    private static void powerEverything(SceneBuildingUtil util, CreateSceneBuilder scene, boolean activate, boolean ignoreLamp) {
        BlockPos redstoneLamp = util.grid().at(3, 1, 0);
        BlockPos acceleratorPos = util.grid().at(3, 3, 2);
        BlockPos entrancePos = util.grid().at(3, 1, 2);
        BlockPos redstonePos = util.grid().at(3, 1, 1);

        int power = activate ? 15 : 0;
        if (!ignoreLamp) {
            scene.world().modifyBlock(redstoneLamp, state -> state.setValue(RedstoneLampBlock.LIT, activate), false);
        }
        changeToPower(acceleratorPos, scene, power);
        changeToPower(entrancePos, scene, power);
        changeToPower(redstonePos, scene, power);
    }

    private static void changeToPower(BlockPos pos, CreateSceneBuilder scene, int power) {
        scene.world().modifyBlock(pos, state -> state.setValue(RedStoneWireBlock.POWER, power), false);
    }

    private static void setSystemSpeed(SceneBuildingUtil util, CreateSceneBuilder scene, int entranceSpeed) {
        Selection mainShaftS = util.select().fromTo(3, 1, 5, 6, 1, 4);
        Selection sideCogS = util.select().position(3, 1, 3);
        Selection baseCog = util.select().position(6, 0, 3);
        Selection tubeS = util.select().position(3, 1, 2);


        scene.world().setKineticSpeed(baseCog, -entranceSpeed * 2);
        scene.world().setKineticSpeed(mainShaftS, entranceSpeed);
        scene.world().setKineticSpeed(sideCogS, -entranceSpeed);
        scene.world().setKineticSpeed(tubeS, entranceSpeed);
    }

    private static void removeAttachment(SceneBuilder builder, BlockPos p1, Direction direction) {
        builder.world().modifyBlockEntity(p1, ActionTubeBlockEntity.class, blockEntity -> {
            if (!blockEntity.hasTubeAttachment(direction)) return;
            blockEntity.removeTubeAttachment(direction);
        });
    }

    private static void changeAttachment(SceneBuilder builder, BlockPos p1, Direction direction, ITubeAttachment tubeAttachment) {
        builder.world().modifyBlockEntity(p1, ActionTubeBlockEntity.class, blockEntity -> {
            blockEntity.addTubeAttachment(direction, tubeAttachment);
        });
    }

    private static void changeOpenCloseEntrance(SceneBuilder builder, BlockPos p1, boolean open) {
        builder.world()
                .modifyBlock(p1, blockState -> blockState.setValue(HyperAcceleratorBlock.OPEN, open), false);
    }
}
