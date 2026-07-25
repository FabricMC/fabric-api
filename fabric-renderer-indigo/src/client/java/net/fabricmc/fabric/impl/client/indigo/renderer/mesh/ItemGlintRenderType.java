package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

/**
 * Allowed values for {@link MutableQuadView#itemGlintRenderType(RenderType)}.
 */
enum ItemGlintRenderType {
	CUTOUT(Sheets.cutoutItemGlintSheet()),
	TRANSLUCENT(Sheets.translucentItemGlintSheet()),
	CUTOUT_BLOCK(Sheets.cutoutBlockItemGlintSheet()),
	TRANSLUCENT_BLOCK(Sheets.translucentBlockItemGlintSheet());

	static final RenderType[] RENDER_TYPES = Arrays.stream(ItemGlintRenderType.values()).map(t -> t.renderType).toArray(RenderType[]::new);
	static final Map<RenderType, ItemGlintRenderType> RENDER_TYPE_2_ENUM;

	static {
		RENDER_TYPE_2_ENUM = Map.of(
				CUTOUT.renderType, CUTOUT,
				TRANSLUCENT.renderType, TRANSLUCENT,
				CUTOUT_BLOCK.renderType, CUTOUT_BLOCK,
				TRANSLUCENT_BLOCK.renderType, TRANSLUCENT_BLOCK
		);
	}

	// The atlas of the default render type should match the default QuadAtlas, which is currently BLOCK.
	static final ItemGlintRenderType DEFAULT = ItemGlintRenderType.CUTOUT_BLOCK;

	final RenderType renderType;

	ItemGlintRenderType(RenderType renderType) {
		this.renderType = renderType;
	}
}
