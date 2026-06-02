package com.pedrorok.hypertube.core.compat;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.LoadingModList;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.Function;

public enum Mods {
	SABLE;

	private final String id;
	private final boolean isLoaded;

	Mods() {
		id = name().toLowerCase();
		isLoaded = LoadingModList.get().getModFileById(id) != null;
	}

	/**
	 * @return the mod id
	 */
	public String id() {
		return id;
	}

	public ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(id, path);
	}

	public Block getBlock(String id) {
		return BuiltInRegistries.BLOCK.get(rl(id));
	}

	public Item getItem(String id) {
		return BuiltInRegistries.ITEM.get(rl(id));
	}

	public boolean contains(ItemLike entry) {
		if (!isLoaded())
			return false;
		Item asItem = entry.asItem();
		return asItem != null && RegisteredObjectsHelper.getKeyOrThrow(asItem)
			.getNamespace()
			.equals(id);
	}

	/**
	 * @return a boolean of whether the mod is loaded or not based on mod id
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * Simple hook to run code if a mod is installed
	 *
	 * @param toRun will be run only if the mod is loaded
	 * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded())
			return Optional.of(toRun.get().get());
		return Optional.empty();
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 *
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded()) {
			toExecute.get().run();
		}
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 *
	 * @param toExecute will be executed only if the mod is loaded
	 * @param parameter will be passed into the scope, or returned if the mod isn't loaded
	 * @return parameter if the mod is not loaded, otherwise the return value of the given function
	 */
	public <T> T executeIfInstalled(Supplier<Function<T, T>> toExecute, T parameter) {
		if (isLoaded()) {
			return toExecute.get().apply(parameter);
		}
		return parameter;
	}
}
