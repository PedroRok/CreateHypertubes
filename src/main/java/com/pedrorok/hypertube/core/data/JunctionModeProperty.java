package com.pedrorok.hypertube.core.data;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. 18/06/2026
 * @project Create Hypertube
 */
public class JunctionModeProperty extends EnumProperty<JunctionMode> {

    protected JunctionModeProperty(String key, Collection<JunctionMode> list) {
        super(key, JunctionMode.class, list);
    }

    public static JunctionModeProperty create(String key) {
        return new JunctionModeProperty(key, List.of(JunctionMode.values()));
    }
    
     public static JunctionModeProperty create(String key, Collection<JunctionMode> allowedValues) {
        return new JunctionModeProperty(key, allowedValues);
    }
}
