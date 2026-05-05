package net.fabricmc.fabric.mixin.holder.component.extension;

import net.minecraft.core.Holder;

import net.minecraft.core.component.DataComponentHolder;

import net.minecraft.core.component.DataComponentMap;

import net.minecraft.world.entity.EntityType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityType.class)
public class EntityTypeMixin implements DataComponentHolder {
	@Shadow
	@Final
	private Holder.Reference<EntityType<?>> builtInRegistryHolder;

	@Override
	public DataComponentMap getComponents() {
		return builtInRegistryHolder.components();
	}
}
