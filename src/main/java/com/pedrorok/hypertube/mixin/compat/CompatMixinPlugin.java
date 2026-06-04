package com.pedrorok.hypertube.mixin.compat;

import com.pedrorok.hypertube.core.compat.Mods;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompatMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, Mods> MOD_FOLDERS = Map.of(
        // mixin.compat."folder", Mods.VALUE
        "sable", Mods.SABLE
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".mixin.compat.")) {
            String[] parts = mixinClassName.split("\\.mixin.compat\\.", 2)[1].split("\\."); // split uses regex, so have to escape all periods
            String path = "";
            for (String s : parts) {
                path += s;
                Mods mod = MOD_FOLDERS.getOrDefault(path, null);
                if (mod != null && mod.isLoaded()) {
                    return true;
                }
                path += ".";
            }
        }
        return false;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}