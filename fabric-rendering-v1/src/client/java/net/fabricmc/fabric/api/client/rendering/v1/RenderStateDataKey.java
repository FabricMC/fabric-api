package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.function.Supplier;

/**
 * A unique key representing extra data to attach to a render state.
 * @param <T> The type of the render state data.
 * @see FabricRenderState#getData(RenderStateDataKey)
 * @see FabricRenderState#setData(RenderStateDataKey, Object)
 */
public final class RenderStateDataKey<T> {
	private final Supplier<String> name;

	private RenderStateDataKey(Supplier<String> debugName) {
		this.name = debugName;
	}

	/**
	 * Creates a new unique data key.
	 * @param debugName The name of this data key, shown in error messages.
	 * @return The newly created data key.
	 * @param <T> The type of the render state data.
	 */
	public static <T> RenderStateDataKey<T> create(Supplier<String> debugName) {
		return new RenderStateDataKey<>(debugName);
	}

	/**
	 * Creates a new unique data key.
	 * @return The newly created data key.
	 * @param <T> The type of the render state data.
	 */
	public static <T> RenderStateDataKey<T> create() {
		return new RenderStateDataKey<>(() -> "unnamed");
	}

	@Override
	public String toString() {
		return "RenderStateDataKey(" + name.get() + ")";
	}
}
