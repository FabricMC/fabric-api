package net.fabricmc.fabric.mixin.holder.component;

import net.minecraft.resources.RegistryOps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor {
	@Accessor
	RegistryOps.RegistryInfoLookup getLookupProvider();
}
