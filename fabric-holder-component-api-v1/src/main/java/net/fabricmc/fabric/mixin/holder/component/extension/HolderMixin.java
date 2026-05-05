package net.fabricmc.fabric.mixin.holder.component.extension;

import net.minecraft.core.Holder;

import net.minecraft.core.component.DataComponentHolder;

import net.minecraft.core.component.DataComponentMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Holder.class)
public interface HolderMixin extends DataComponentHolder {
	@Shadow
	DataComponentMap components();

	@Override
	default DataComponentMap getComponents() {
		return components();
	}
}
