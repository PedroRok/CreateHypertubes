package com.pedrorok.hypertube.ponder.scenes;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. 27/01/2026
 * @project Create Hypertube
 */
public class TubeScenes {

    private static final Map<String, IConnection> cachedConnection = new HashMap<>();

    public static void simpleTube(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        cacheBlocks(scene, util);

        scene.title("simple_tube", "Creating a Hypertube");
        scene.configureBasePlate(0, 0, 7);
        scene.setSceneOffsetY(0);

        // Show base
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.idle(5);

        BlockPos block1 = util.grid().at(5, 1, 2);
        BlockPos block2 = util.grid().at(2, 1, 5);

        Selection block1S = util.select()
                .position(block1);
        Selection block2S = util.select()
                .position(block2);


        setConnection(scene, block1, "sec-1", false);

        scene.world().showSection(block1S, Direction.DOWN);
        scene.idle(10);
        ItemStack tubeItem = ModBlocks.HYPERTUBE.asStack();

        scene.overlay()
                .showText(70)
                .text("To create a Hypertube connection, Just Right Click the tube while holding another tube.")
                .attachKeyFrame()
                .pointAt(block1.getCenter().add(0, -0.6, 0.3))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay()
                .showControls(block1.getCenter().add(0, 0, 0.3), Pointing.LEFT, 40)
                .rightClick()
                .withItem(tubeItem);
        scene.idle(70);


        scene.overlay()
                .showControls(block2.getCenter().add(0, 0, 0), Pointing.DOWN, 50)
                .rightClick()
                .withItem(tubeItem);
        scene.idle(5);
        scene.overlay()
                .showText(40)
                .text("Than place the second tube and the connection will be created.")
                .attachKeyFrame()
                .pointAt(block2.getCenter().add(0, -0.5, 0.3))
                .placeNearTarget();
        scene.idle(5);

        scene.world().showSection(block2S, Direction.NORTH);
        scene.idle(10);
        setConnection(scene, block1, "sec-1", true);
        scene.idle(40);

        scene.overlay()
                .showText(60)
                .text("And you have a Hypertube connection!")
                .pointAt(new Vec3(3, 2, 3))
                .colored(PonderPalette.GREEN)
                .independent(8);
        scene.idle(65);
        scene.world().hideSection(block2S, Direction.UP);
        scene.world().hideSection(block1S, Direction.UP);
        scene.idle(20);

        // SECOND SECTION
        BlockPos blockL1 = util.grid().at(6, 1, 1);
        BlockPos blockL2 = util.grid().at(3, 1, 1);

        BlockPos blockR1 = util.grid().at(1, 1, 3);
        BlockPos blockR2 = util.grid().at(1, 1, 6);


        Selection blockL1S = util.select()
                .position(blockL1);
        Selection blockL2S = util.select()
                .position(blockL2);
        Selection blockR1S = util.select()
                .position(blockR1);
        Selection blockR2S = util.select()
                .position(blockR2);

        setConnection(scene, blockL2, "sec-2-l2", false);

        scene.addKeyframe();

        scene.world().showSection(blockL1S, Direction.DOWN);
        scene.world().showSection(blockL2S, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(blockR1S, Direction.DOWN);
        scene.world().showSection(blockR2S, Direction.DOWN);
        scene.idle(20);

        scene.overlay()
                .showControls(blockL2.getCenter().add(0, 0, 0.3), Pointing.LEFT, 15)
                .rightClick()
                .withItem(ModBlocks.HYPERTUBE.asStack());
        scene.idle(25);
        scene.overlay()
                .showControls(blockR1.getCenter().add(0, 0, -0.3), Pointing.RIGHT, 15)
                .rightClick()
                .withItem(ModBlocks.HYPERTUBE.asStack());
        scene.idle(20);
        setConnection(scene, blockL2, "sec-2-l2", true);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("You can connect already placed tubes.")
                .colored(PonderPalette.GREEN)
                .pointAt(new Vec3(1, 3, 1))
                .independent();
        scene.idle(80);
        scene.addKeyframe();
        scene.overlay()
                .showControls(blockR1.getCenter().add(0, 0.5, 0), Pointing.DOWN, 15)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(10);
        changeSegmentCount(scene, blockL2, "sec-2-l2");
        changeSegmentCount(scene, blockR2, "sec-2-r2");
        scene.idle(30);
        scene.overlay()
                .showText(70)
                .text("You can change tube segment count with the wrench.")
                .pointAt(new Vec3(1, 3, 1))
                .independent();
        scene.overlay()
                .showControls(blockL1.getCenter().add(0, 0, 0.3), Pointing.LEFT, 15)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(10);
        changeSegmentCount(scene, blockL1, "sec-2-l1");
        scene.idle(70);

        BlockPos lastBlock = util.grid().at(4, 1, 5);
        changeSegmentCount(scene, lastBlock, "sec-3");
        scene.world().showSection(util.select().fromTo(4, 1, 4, 5, 6, 5), Direction.DOWN);

    }

    private static void cacheBlocks(CreateSceneBuilder scene, SceneBuildingUtil util) {
        cachedConnection.clear();

        // First Section
        BlockPos firstBlock1 = util.grid().at(5, 1, 2);
        cacheConnection(scene, firstBlock1, "sec-1");

        // Second Section
        BlockPos secondBlockL1 = util.grid().at(6, 1, 1);
        BlockPos secondBlockL2 = util.grid().at(3, 1, 1);
        BlockPos secondBlockR2 = util.grid().at(1, 1, 6);
        cacheConnection(scene, secondBlockL1, "sec-2-l1");
        cacheConnection(scene, secondBlockL2, "sec-2-l2");
        cacheConnection(scene, secondBlockR2, "sec-2-r2");

        // Last Block
        BlockPos lastBlock = util.grid().at(5, 1, 4);
        cacheConnection(scene, lastBlock, "sec-3");
    }

    private static void cacheConnection(SceneBuilder builder, BlockPos p1, String key) {
        builder.world()
                .modifyBlockEntity(p1, HypertubeBlockEntity.class, be -> {
                    IConnection connectionOne = be.getConnectionOne();
                    if (connectionOne == null) {
                        HypertubeMod.LOGGER.error("Connection One is null when caching connection for key: {}", key);
                        return;
                    }
                    if (connectionOne instanceof SimpleConnection) {
                        HypertubeMod.LOGGER.error("Connection One is SimpleConnection when caching connection for key: {}", key);
                        return;
                    }
                    cachedConnection.put(key, connectionOne);
                });
    }

    private static void setConnection(SceneBuilder builder, BlockPos p1, String key, boolean connect) {
        builder.world()
                .modifyBlockEntity(p1, HypertubeBlockEntity.class, be -> {
                    IConnection connection = cachedConnection.get(key);
                    if (connect) {
                        be.setConnection(connection, connection.getThisConnection().direction());
                    } else {
                        be.clearConnection(connection);
                    }
                });
    }


    private static void changeSegmentCount(SceneBuilder builder, BlockPos p1, String key) {
        builder.world()
                .modifyBlockEntity(p1, HypertubeBlockEntity.class, be -> {
                    IConnection connection = cachedConnection.get(key);
                    connection.updateTubeSegments(be.getLevel());
                });
    }
}
