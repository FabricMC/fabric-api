package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.resources.RegistryOps;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor {
    @Accessor("lookupProvider")
    RegistryOps.RegistryInfoLookup getLookupProvider();
}
