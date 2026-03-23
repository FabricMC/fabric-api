package net.fabricmc.fabric.api.client.renderer.v1.render;

import java.util.Collection;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;

/**
 * Note: This interface is automatically implemented on {@link net.minecraft.client.renderer.feature.BlockFeatureRenderer} via Mixin and interface injection.
 */
public interface FabricBlockFeatureRenderer {
	/**
	 * This method allows buffering a block model with minimal transformations to the model geometry.
	 * Usually used by entity renderers.
	 */
	static void putModelQuads(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, BlockStateModel model, int tintColor, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
	}

	/**
	 * This method allows buffering a {@link BlockStateModelPart} with minimal transformations to
	 * the model geometry. Usually used by entity renderers.
	 */
	static void putPartQuads(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, Collection<BlockStateModelPart> blockStateModelPart, int tintColor, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		Renderer.get()
				.putPartQuads();
	}
}
