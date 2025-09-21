package net.fabricmc.fabric.mixin.blockview.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;

@Mixin(MovingBlockRenderState.class)
abstract class MovingBlockRenderStateMixin implements BlockRenderView {
	@Shadow
	@Nullable
	public RegistryEntry<Biome> biome;

	@Override
	public boolean hasBiomes() {
		return biome != null;
	}

	@Override
	public RegistryEntry<Biome> getBiomeFabric(BlockPos pos) {
		return biome;
	}
}
