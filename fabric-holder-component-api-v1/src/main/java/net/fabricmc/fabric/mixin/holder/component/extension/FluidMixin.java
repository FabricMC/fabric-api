package net.fabricmc.fabric.mixin.holder.component.extension;

import net.minecraft.core.Holder;

import net.minecraft.core.component.DataComponentHolder;

import net.minecraft.core.component.DataComponentMap;

import net.minecraft.world.level.material.Fluid;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Fluid.class)
public class FluidMixin implements DataComponentHolder {
	@Shadow
	@Final
	private Holder.Reference<Fluid> builtInRegistryHolder;

	@Override
	public DataComponentMap getComponents() {
		return builtInRegistryHolder.components();
	}
}
