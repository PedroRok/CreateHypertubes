package com.pedrorok.hypertube.managers.ponder.scenes;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class HypertubeScenes {

    public static void tube(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("hypertube", "Placing and using Hypertubes");
        scene.configureBasePlate(0, 0, 6);
        scene.world()
                .showSection(util.select()
                        .layer(0), Direction.UP);

        Selection exitConnSelection = util.select()
                .fromTo(1, 1, 4, 2, 2, 4);
        Selection entranceSelection = util.select()
                .fromTo(4, 1, 1, 4, 1, 1);
        Selection entryConnSelection = util.select()
                .fromTo(4, 1, 1, 4, 1, 2);
        Selection powerShaft = util.select()
                .fromTo(1, 1, 0, 5, 1, 0);

        BlockPos entrance = util.grid()
                .at(1, 1, 1);
        BlockPos connection1 = util.grid()
                .at(4, 1, 2);
        BlockPos connection2 = util.grid()
                .at(2, 1, 4);

        //connection(builder, conv1, conv2, false);
        //connection(builder, conv1, conv3, false);
        //connection(builder, conv1, conv4, false);

        scene.world()
                .setKineticSpeed(entranceSelection, 0);

        scene.idle(5);
        scene.world()
                .showSection(entryConnSelection, Direction.DOWN);
        scene.idle(5);
        scene.world()
                .showSection(exitConnSelection, Direction.DOWN);
        scene.idle(40);

        ItemStack tubeItem = new ItemStack(ModBlocks.HYPERTUBE.asItem());
        scene.overlay()
                .showControls(util.vector()
                        .topOf(connection1), Pointing.DOWN, 117)
                .rightClick()
                .withItem(tubeItem);

        Vec3 c1 = util.vector()
                .centerOf(connection1);
        AABB bb1 = new AABB(c1, c1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, connection1, bb1, 10);
        scene.idle(1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, connection1, bb1, 117);
        scene.idle(16);

        scene.overlay()
                .showControls(util.vector()
                        .topOf(connection2), Pointing.RIGHT, 100)
                .rightClick()
                .withItem(tubeItem);

        scene.idle(16);


        /*Vec3 c2 = util.vector()
                .centerOf(connection2);
        AABB bb2 = new AABB(c2, c2);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, connection2, bb2, 10);
        scene.idle(1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, connection2, bb2.inflate(1, 0.5, 1), 100);
        scene.idle(10);*/

        //connection(builder, conv1, conv2, true);
        //scene.world()
        //        .setKineticSpeed(conv2S, -32);

        /*scene.overlay()
                .showText(80)
                .text("Right-click one Hypertubes than another to connect them")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .topOf(connection1.offset(-1, 0, -1)));
        scene.idle(90);*/

        /*scene.world()
                .showSection(pole3, Direction.DOWN);
        scene.idle(3);
        scene.world()
                .showSection(pole4, Direction.DOWN);
        scene.idle(6);
        scene.world()
                .showSection(conv3S, Direction.DOWN);
        scene.idle(3);
        scene.world()
                .showSection(conv4S, Direction.DOWN);
        scene.idle(12);
        connection(builder, conv1, conv3, true);
        scene.idle(3);
        connection(builder, conv1, conv4, true);
        scene.idle(20);

        scene.overlay()
                .showText(70)
                .text("Chain conveyors relay rotational power between each other..")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .topOf(conv3.offset(-1, 0, -1)));
        scene.idle(60);

        scene.world()
                .hideIndependentSection(poleE, Direction.SOUTH);
        scene.idle(20);
        scene.world()
                .showSection(cogsAbove, Direction.DOWN);
        scene.idle(3);
        scene.world()
                .showSection(cogsBelow, Direction.UP);
        scene.idle(12);

        scene.effects()
                .rotationDirectionIndicator(conv2.above());
        scene.idle(3);
        scene.effects()
                .rotationDirectionIndicator(conv2.below(2));
        scene.idle(10);

        scene.overlay()
                .showText(60)
                .text("..and connect to shafts above or below them")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .centerOf(util.grid()
                                .at(1, 2, 7)));
        scene.idle(60);
        scene.world()
                .hideSection(cogsBelow, Direction.SOUTH);
        scene.idle(15);
        ElementLink<WorldSectionElement> poleE2 = scene.world()
                .showIndependentSection(pole, Direction.EAST);
        scene.world()
                .moveSection(poleE2, util.vector()
                        .of(0, 0, 1), 0);
        scene.idle(10);

        scene.overlay()
                .showText(80)
                .text("Right-click holding a wrench to start travelling on the chain")
                .attachKeyFrame()
                .independent(30);

        scene.idle(40);
        ElementLink<ParrotElement> parrot = new ElementLinkImpl<>(ParrotElement.class);
        Vec3 parrotStart = util.vector()
                .centerOf(conv2)
                .add(0, -1.45, 1);
        FrogAndConveyorScenes.ChainConveyorParrotElement element =
                new FrogAndConveyorScenes.ChainConveyorParrotElement(parrotStart, ParrotPose.FacePointOfInterestPose::new);
        scene.addInstruction(new CreateParrotInstruction(0, Direction.DOWN, element));
        scene.addInstruction(s -> s.linkElement(element, parrot));
        scene.special()
                .movePointOfInterest(util.grid()
                        .at(0, 3, 2));

        scene.idle(20);
        scene.special()
                .moveParrot(parrot, util.vector()
                        .of(-1, 0, -1), 14);
        scene.idle(14);
        scene.special()
                .movePointOfInterest(util.grid()
                        .at(7, 3, 0));
        scene.special()
                .moveParrot(parrot, util.vector()
                        .of(5.75, 0, -5.75), 90);
        scene.idle(65);

        scene.overlay()
                .showText(60)
                .text("At a junction, face towards a chain to follow it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .topOf(conv1));

        scene.idle(25);
        scene.special()
                .movePointOfInterest(util.grid()
                        .at(9, 3, 1));
        scene.special()
                .moveParrot(parrot, util.vector()
                        .of(1, 0, -1), 14);
        scene.idle(14);
        scene.special()
                .movePointOfInterest(util.grid()
                        .at(9, 3, 3));
        scene.special()
                .moveParrot(parrot, util.vector()
                        .of(0.5, 0, 0), 6);
        scene.idle(6);
        scene.special()
                .movePointOfInterest(util.grid()
                        .at(8, 3, 10));
        scene.special()
                .moveParrot(parrot, util.vector()
                        .of(0.5, 0, 0.5), 14);
        scene.idle(14);
        scene.special()
                .moveParrot(parrot, util.vector()
                        .of(0, 0, 7), 78);
        scene.idle(78);
        scene.special()
                .hideElement(parrot, Direction.SOUTH);*/
    }

    /*private static void connection(SceneBuilder builder, BlockPos p1, BlockPos p2, boolean connect) {
        builder.world()
                .modifyBlockEntity(p1, HypertubeBlockEntity.class, be -> {
                    if (connect)
                        be.connections.add(p2.subtract(p1));
                    else
                        be.connections.remove(p2.subtract(p1));
                });
        builder.world()
                .modifyBlockEntity(p2, HypertubeBlockEntity.class, be -> {
                    if (connect)
                        be.connections.add(p1.subtract(p2));
                    else
                        be.connections.remove(p1.subtract(p2));
                });
    }*/
}
