package net.fabricmc.fabric.mixin.client.rendering.fluid;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingRegistryImpl;

@Mixin(FluidStateModelSet.class)
public abstract class FluidStateModelSetMixin {
	@WrapMethod(method = "bake")
	private static Map<Fluid, FluidModel> bake(MaterialBaker materials, Operation<Map<Fluid, FluidModel>> original) {
		Map<Fluid, FluidModel> models = new IdentityHashMap<>(original.call(materials));
		Map<FluidModel.Unbaked, FluidModel> bakedModels = new IdentityHashMap<>();

		for (Map.Entry<Fluid, FluidModel.Unbaked> entry : FluidRenderingRegistryImpl.getUnbakedModels().entrySet()) {
			FluidModel model = bakedModels.get(entry.getValue());

			if (model == null) {
				model = entry.getValue().bake(materials, () -> entry.getKey().toString());
				bakedModels.put(entry.getValue(), model);
			}

			models.put(entry.getKey(), model);
		}

		return Collections.unmodifiableMap(models);
	}
}
