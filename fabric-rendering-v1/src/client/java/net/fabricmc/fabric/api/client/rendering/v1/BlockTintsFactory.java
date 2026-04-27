package net.fabricmc.fabric.api.client.rendering.v1;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This factory takes over the collection of tint colors in a model renderer.
 * <p>
 *     If this factory provides any tints in the tintValues collections of its {@link #collect(BlockState, BlockAndTintGetter, BlockPos, IntList)}
 *     method then the default vanilla behaviour of iterating the registered {@link net.minecraft.client.color.block.BlockTintSource block tint sources}
 *     is skipped.
 * </p>
 * <p>
 *     This factory is only invoked if no {@link net.minecraft.client.color.block.BlockTintSource tint source} has been registered for the {@link BlockState block state}.
 * </p>
 */
@FunctionalInterface
public interface BlockTintsFactory
{
	/**
	 * Invoked to collect the dynamic tint values for the given block state.
	 * <p>
	 *     The tint applied to a given {@link net.minecraft.client.resources.model.geometry.BakedQuad quad}
	 *     is then determined based on the index stored in {@link BakedQuad.MaterialInfo#tintIndex()} by looking
	 *     them up in the tint values list after this collect method is called.
	 * </p>
	 * <p>
	 *     The resulting tints might be cached for this state, level and position, while the
	 *     given position and model are rendered, but may not be stored beyond that time window,
	 *     especially not beyond any given frame being rendered.
	 * </p>
	 * <p>
	 *     The given tint list is guaranteed to be empty.
	 *     It is recommended to call the {@link IntList#size(int) size} method if you at the start of the method, ahead of time, how many
	 *     tints your system will eventually register as this will pre-allocate enough memory to hold your ints.
	 * </p>
	 *
	 * @param state The state for which the tints are retrieved.
	 * @param level The level in which they are retrieved.
	 * @param pos The position inside the level for which they are retrieved.
	 * @param tintValues The target collection in which to store the tint values for the given index.
	 */
	void collect(BlockState state, BlockAndTintGetter level, BlockPos pos, IntList tintValues);
}
