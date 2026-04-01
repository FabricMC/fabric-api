package net.fabricmc.fabric.mixin.tag;

import net.fabricmc.fabric.impl.tag.TagLoaderEntryWithSourceHooks;

import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TagLoader.EntryWithSource.class)
public class TagLoaderEntryWithSourceMixin implements TagLoaderEntryWithSourceHooks {
	@Unique
	private boolean remove;

	@Override
	public boolean fabric_remove() {
		return remove;
	}

	@Override
	public void fabric_setRemove(boolean value) {
		remove = value;
	}
}
