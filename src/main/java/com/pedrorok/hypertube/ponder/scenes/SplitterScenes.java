package com.pedrorok.hypertube.ponder.scenes;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.data.JunctionMode;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.pedrorok.hypertube.items.TubeAttachmentItem;
import com.pedrorok.hypertube.ponder.elements.TubePulsePonderElement;
import com.pedrorok.hypertube.registry.ModItems;
import com.pedrorok.hypertube.utils.ModColors;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. 29/07/2026
 * @project Create Hypertube
 */
public class SplitterScenes {

    private static final double BIRB_Y = 1.1;

    public static void splitterScene(SceneBuilder scene, SceneBuildingUtil util) {
        final TubeAttachmentItem redstoneDetectorItem = ModItems.REDSTONE_DETECTOR.get();

        scene.title("splitter", "Hypertube Splitter");
        scene.configureBasePlate(0, 0, 7);
        scene.setSceneOffsetY(0);

        BlockPos splitterPos = util.grid.at(4, 1, 3);

        Selection tubesS = util.select.layer(1);

        Vec3 splitterCenter = util.vector.of(4.5, 1.5, 3.5);
        Vec3 splitterDialogPos = splitterCenter.add(-1.2, 1.35, -0.5);
        Vec3 fromWest = util.vector.of(0, BIRB_Y, 3.5);
        Vec3 fromNorth = util.vector.of(4.5, BIRB_Y, 0);

        // Show base
        scene.world.showSection(util.select.layer(0), Direction.UP);
        scene.idle(5);
        scene.world.showSection(tubesS, Direction.DOWN);
        scene.idle(20);

        scene.overlay
                .showText(80)
                .attachKeyFrame()
                .pointAt(splitterDialogPos)
                .placeNearTarget()
                .text("The Hypertube Splitter can link up to three tubes, branching a line into multiple paths.");
        scene.idle(90);


        // AUTOMATIC MODE
        scene.overlay
                .showText(150)
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(splitterDialogPos)
                .placeNearTarget()
                .text("While traveling, you can pick where to go with your movement keys.");
        scene.idle(20);

        // right branchs
        travel(scene, splitterPos, fromWest, new Vec3(4.5, 0, 0), 30,
                new Vec3(0, 0, 4), 25, Direction.SOUTH, ModColors.GREEN);
        scene.idle(10);
        // left branch
        travel(scene, splitterPos, fromWest, new Vec3(4.5, 0, 0), 30,
                new Vec3(0, 0, -4), 25, Direction.NORTH, ModColors.GREEN);
        scene.idle(20);

        // FORCED CONTINUE MODE
        scene.overlay
                .showText(85)
                .attachKeyFrame()
                .pointAt(splitterDialogPos)
                .placeNearTarget()
                .text("Right Click it with a Wrench to cycle through the Splitter modes.");
        scene.idle(15);
        scene.overlay
                .showControls(
                        new InputWindowElement(splitterPos.getCenter().add(0, 1.1, -1), Pointing.RIGHT)
                                .rightClick()
                                .withItem(AllItems.WRENCH.asStack()), 30);
        scene.idle(10);
        setMode(scene, splitterPos, JunctionMode.FORCED_CONTINUE);
        scene.effects.indicateSuccess(splitterPos);
        scene.idle(60);

        scene.overlay
                .showText(100)
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(splitterDialogPos)
                .placeNearTarget()
                .text("On Continue mode, travelers always keep going through the side tubes.");
        scene.idle(20);
        travel(scene, splitterPos, fromNorth, new Vec3(0, 0, 3.5), 30,
                new Vec3(0, 0, 4), 25, Direction.SOUTH, ModColors.ORANGE);
        scene.idle(20);

        // FORCED CENTER MODE
        scene.overlay
                .showControls(
                        new InputWindowElement(splitterPos.getCenter().add(0, 1.1, -1), Pointing.RIGHT)
                                .rightClick()
                                .withItem(AllItems.WRENCH.asStack()), 30);
        scene.idle(10);
        setMode(scene, splitterPos, JunctionMode.FORCED_CENTER);
        scene.effects.indicateSuccess(splitterPos);
        scene.idle(20);

        scene.overlay
                .showText(100)
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(splitterDialogPos)
                .placeNearTarget()
                .text("On Center mode, travelers are always routed through the front tube.");
        scene.idle(20);
        travel(scene, splitterPos, fromNorth, new Vec3(0, 0, 3.5), 30,
                new Vec3(-5, 0, 0), 25, Direction.WEST, ModColors.ORANGE);
        scene.idle(40);

        scene.rotateCameraY(180);

        scene.idle(40);

        // REDSTONE
        scene.overlay
                .showControls(
                        new InputWindowElement(splitterPos.getCenter().add(0.5, -0.5, 0), Pointing.UP)
                                .rightClick()
                                .withItem(redstoneDetectorItem.getDefaultInstance()), 40);
        scene.idle(10);
        changeAttachment(scene, splitterPos, Direction.EAST, redstoneDetectorItem.getTubeAttachment());
        scene.idle(5);
        scene.overlay
                .showText(80)
                .attachKeyFrame()
                .pointAt(splitterCenter.add(0.9, 0.4, 0))
                .placeNearTarget()
                .text("A Redstone Detector Attachment can toggle between both forced modes.");
        scene.idle(90);
    }

    private static void travel(SceneBuilder scene, BlockPos splitterPos, Vec3 from, Vec3 toSplitter, int enterTicks, Vec3 toExit, int exitTicks, Direction exitFace, int pulseColor) {
        showPulse(scene, splitterPos, exitFace, pulseColor, 50);
        ElementLink<ParrotElement> birb = scene.special
                .createBirb(from, ParrotElement.DancePose::new);
        scene.special.moveParrot(birb, toSplitter, enterTicks);
        scene.idle(enterTicks - 15);
        scene.idle(10);
        changeOpenClose(scene, splitterPos, true);
        scene.idle(5);
        scene.special.moveParrot(birb, toExit, exitTicks);
        scene.idle(10);
        changeOpenClose(scene, splitterPos, false);
        scene.idle(exitTicks - 10);
        scene.special.hideElement(birb, exitFace);
    }

    private static void showPulse(SceneBuilder scene, BlockPos splitterPos, Direction exitFace, int color, int durationTicks) {
        scene.addInstruction(ponderScene -> {
            PonderWorld level = ponderScene.getWorld();
            if (!(level.getBlockEntity(splitterPos) instanceof HyperJunctionBlockEntity splitter)) return;

            IConnection connection = splitter.getConnectionInDirection(exitFace);
            if (connection == null) return;
            BezierConnection bezier = connection.getThisEntranceConnection(level);
            if (bezier == null) return;

            TubePulsePonderElement pulse = TubePulsePonderElement.of(bezier, splitterPos, color, durationTicks);
            if (pulse == null) return;
            ponderScene.addElement(pulse);
        });
    }

    private static void setMode(SceneBuilder scene, BlockPos pos, JunctionMode mode) {
        scene.world
                .modifyBlock(pos, blockState -> blockState.setValue(HyperJunctionBlock.JUNCTION_MODE, mode), false);
    }

    private static void changeOpenClose(SceneBuilder scene, BlockPos pos, boolean open) {
        scene.world
                .modifyBlock(pos, blockState -> blockState.setValue(HyperJunctionBlock.OPEN, open), false);
    }

    private static void changeAttachment(SceneBuilder builder, BlockPos pos, Direction direction, ITubeAttachment tubeAttachment) {
        builder.world.modifyBlockEntity(pos, ActionTubeBlockEntity.class, blockEntity -> {
            blockEntity.addTubeAttachment(direction, tubeAttachment);
        });
    }
}
