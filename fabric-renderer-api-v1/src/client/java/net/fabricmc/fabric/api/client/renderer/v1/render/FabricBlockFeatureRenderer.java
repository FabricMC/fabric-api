package net.fabricmc.fabric.api.client.renderer.v1.render;

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;

/**
 * Note: This interface is automatically implemented on {@link BlockFeatureRenderer} via Mixin and interface injection.
 */
public interface FabricBlockFeatureRenderer {
	/**
	 * This method allows buffering a block model with minimal transformations to the model geometry.
	 * Usually used by entity renderers.
	 *
	 * @see FabricOrderedSubmitNodeCollector#submitBlockModel(PoseStack, Function, BlockStateModel, int[], int, int, int, BlockAndTintGetter, BlockPos, BlockState)
	 */
	static void putModelQuads(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, Predicate<ChunkSectionLayer> layerFilter, BlockStateModel model, int[] tintLayers, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		Renderer.get()
				.putModelQuads(pose, bufferSource, layerFilter, model, tintLayers, light, overlay, level, pos, state);
	}

	/**
	 * This method allows buffering a block model with minimal transformations to the model geometry.
	 * Usually used by entity renderers.
	 *
	 * @see #putModelQuads(PoseStack.Pose, BlockMultiBufferSource, Predicate, BlockStateModel, int[], int, int, BlockAndTintGetter, BlockPos, BlockState)
	 */
	static void putModelQuads(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, BlockStateModel model, int[] tintLayers, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		putModelQuads(pose, bufferSource, null, model, tintLayers, light, overlay, level, pos, state);
	}

	/**
	 * This method allows buffering a block model with minimal transformations to the model geometry.
	 * Usually used by entity renderers.
	 *
	 * @see FabricOrderedSubmitNodeCollector#submitBlockModel(PoseStack, Function, RenderType, RandomSource, MeshView, int[], int, int, int, BlockAndTintGetter, BlockPos, BlockState)
	 */
	static void putMeshQuads(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, Predicate<ChunkSectionLayer> layerFilter, MeshView mesh, int[] tintLayers, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		Renderer.get()
				.putMeshQuads(pose, bufferSource, layerFilter, mesh, tintLayers, light, overlay, level, pos, state);
	}

	/**
	 * This method allows buffering a block model with minimal transformations to the model geometry.
	 * Usually used by entity renderers.
	 *
	 * @see #putMeshQuads(PoseStack.Pose, BlockMultiBufferSource, Predicate, MeshView, int[], int, int, BlockAndTintGetter, BlockPos, BlockState)
	 */
	static void putMeshQuads(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, MeshView mesh, int[] tintLayers, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		putMeshQuads(pose, bufferSource, null, mesh, tintLayers, light, overlay, level, pos, state);
	}
}
