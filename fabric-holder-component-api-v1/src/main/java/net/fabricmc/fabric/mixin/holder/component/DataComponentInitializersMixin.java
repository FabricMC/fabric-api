package net.fabricmc.fabric.mixin.holder.component;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;

import net.fabricmc.fabric.api.holder.component.FabricDataComponentInitializer;
import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitContextImpl;

import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitializersImpl;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(DataComponentInitializers.class)
public class DataComponentInitializersMixin {
	@Unique
	private static final Logger LOGGER = LogUtils.getLogger();

	@ModifyReturnValue(method = "runInitializers", at = @At("RETURN"))
	private Map<ResourceKey<?>, DataComponentMap.Builder> runFabricInitializers(
			Map<ResourceKey<?>, DataComponentMap.Builder> original,
			@Local(name = "context", argsOnly = true) HolderLookup.Provider holders
	) {
		if (!FabricDataComponentInitializersImpl.RESOURCE_MANAGER.isBound()) {
			// we got called either by the report or by a mod
			// if a mod called this, it is doing something cursed, and cant get our components
			// if this is the component report, it didnt need our components
			// TODO: More descriptive error
			LOGGER.warn("DataComponentInitializers.runInitializers() was called, but RESOURCE_MANAGER is not bound!");
			return original;
		}

		ResourceManager resourceManager = FabricDataComponentInitializersImpl.RESOURCE_MANAGER.get();

		FabricDataComponentInitializer.Context context = new FabricDataComponentInitContextImpl(
				holders,
				resourceManager,
				original
		);

		for (FabricDataComponentInitializer initializer : FabricDataComponentInitializersImpl.INITIALIZERS) {
			initializer.run(context);
		}

		return original;
	}
}
