package net.fabricmc.fabric.mixin.holder.component.extension;

import net.minecraft.core.Holder;

import net.minecraft.core.component.DataComponentHolder;

import net.minecraft.core.component.DataComponentMap;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class ItemMixin implements DataComponentHolder {
	@Shadow
	@Final
	private Holder.Reference<Item> builtInRegistryHolder;

	@Override
	public DataComponentMap getComponents() {
		return builtInRegistryHolder.components();
	}
}
