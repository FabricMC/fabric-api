package net.fabricmc.fabric.api.client.renderer.v1.render;

import java.util.function.Predicate;

import org.joml.Matrix4fc;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link BlockModelRenderState} via Mixin and interface injection.
 */
public interface FabricBlockModelRenderState {
	/**
	 * Alternative to {@link BlockModelRenderState#setupModel(Matrix4fc, boolean)} that returns a
	 * {@link QuadEmitter}.
	 *
	 * <p>This method should be used in favor of the vanilla one in order to support modded models.
	 * <b>The vanilla method is therefore considered deprecated.</b>
	 *
	 * @return an emitter to use with {@link BlockStateModel#emitQuads}.
	 * @see BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)
	 */
	default QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
		throw new IllegalStateException("Implemented via Mixin.");
	}
}
