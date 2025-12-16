package net.fabricmc.fabric.api.renderer.v1.render;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;

@FunctionalInterface
public interface ItemLayerRenderTypeGetter {
	/**
	 * Gets the {@link RenderType} from the given {@link QuadAtlas} and
	 * {@link ChunkSectionLayer}.
	 *
	 * <p>Returning {@code null} means a default {@link RenderType} determined
	 * by the renderer will be used.
	 */
	@Nullable RenderType renderType(QuadAtlas quadAtlas, @Nullable ChunkSectionLayer sectionLayer);
}
