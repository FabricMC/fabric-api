package net.fabricmc.fabric.impl.client.renderer;

import java.util.List;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

public record PartitionedBlockStateModel(
		List<BlockStateModelPart> parts,
		@BakedQuad.MaterialFlags int materialFlags
) implements BlockStateModel {
	public PartitionedBlockStateModel(List<BlockStateModelPart> parts) {
		@BakedQuad.MaterialFlags int flags = 0;

		for (BlockStateModelPart part : parts) {
			flags |= part.materialFlags();
		}

		this(parts, flags);
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		output.addAll(this.parts);
	}

	// cursed but nothing really uses it anyway
	@Override
	public Material.Baked particleMaterial() {
		return this.parts.getFirst().particleMaterial();
	}
}
