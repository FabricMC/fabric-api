package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

/**
 * Allowed values for {@link MutableQuadView#itemGlintSpecialRenderType(RenderType)}.
 */
enum ItemGlintSpecialRenderType {
	CUTOUT(Sheets.cutoutItemGlintSpecialSheet()),
	TRANSLUCENT(Sheets.translucentItemGlintSpecialSheet()),
	CUTOUT_BLOCK(Sheets.cutoutBlockItemGlintSpecialSheet()),
	TRANSLUCENT_BLOCK(Sheets.translucentBlockItemGlintSpecialSheet());

	static final RenderType[] RENDER_TYPES = Arrays.stream(ItemGlintSpecialRenderType.values()).map(t -> t.renderType).toArray(RenderType[]::new);
	static final Map<RenderType, ItemGlintSpecialRenderType> RENDER_TYPE_2_ENUM;

	static {
		RENDER_TYPE_2_ENUM = Map.of(
				CUTOUT.renderType, CUTOUT,
				TRANSLUCENT.renderType, TRANSLUCENT,
				CUTOUT_BLOCK.renderType, CUTOUT_BLOCK,
				TRANSLUCENT_BLOCK.renderType, TRANSLUCENT_BLOCK
		);
	}

	// The atlas of the default render type should match the default QuadAtlas, which is currently BLOCK.
	static final ItemGlintSpecialRenderType DEFAULT = ItemGlintSpecialRenderType.CUTOUT_BLOCK;

	final RenderType renderType;

	ItemGlintSpecialRenderType(RenderType renderType) {
		this.renderType = renderType;
	}
}
