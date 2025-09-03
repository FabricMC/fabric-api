package net.fabricmc.fabric.api.client.rendering.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.class_11954;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;

/**
 * Fabric-provided extensions for render states, allowing for the addition of extra render data.
 * <p>Note: This interface is automatically implemented on {@link EntityRenderState},
 * {@link class_11954}, {@link ItemRenderState} and {@link ItemRenderState.LayerRenderState}
 * via Mixin and interface injection.
 */
public interface FabricRenderState {

	/**
	 * Get extra render data from the render state.
	 * @param key the key of the data
	 * @return the data, or {@code null} if it cannot be found.
	 * @param <T> the type of the data
	 */
	@Nullable
	default <T> T getData(RenderStateDataKey<T> key) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Set extra render data to the render state.
	 * @param key the key of the data
	 * @param value the data
	 * @param <T> the type of the data
	 */
	default <T> void setData(RenderStateDataKey<T> key, T value) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Clears all extra render data on the render state.
	 */
	default void clearData() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

}
