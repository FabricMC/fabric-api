package net.fabricmc.fabric.mixin.debug.client;

import net.fabricmc.fabric.impl.debug.client.DebugScreenEntryRegistryImpl;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DebugScreenEntries.class)
public class DebugScreenEntriesMixin {

	@Final
	@Shadow
	private static Map<Identifier, DebugScreenEntry> ENTRIES_BY_ID;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void addDebugEntry(CallbackInfo ci) {
		DebugScreenEntryRegistryImpl.addEntries(ENTRIES_BY_ID);
	}
}
