package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import java.util.Map;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

/**
 * Allowed values for {@link MutableQuadView#itemRenderType(RenderType)}.
 */
public enum ItemRenderType {
	CUTOUT(Sheets.cutoutItemSheet()),
	TRANSLUCENT(Sheets.translucentItemSheet()),
	CUTOUT_BLOCK(Sheets.cutoutBlockItemSheet()),
	TRANSLUCENT_BLOCK(Sheets.translucentBlockItemSheet());

	final RenderType renderType;
	static final Map<RenderType, ItemRenderType> RENDER_TYPE_2_ENUM;

	ItemRenderType(RenderType renderType) {
		this.renderType = renderType;
	}

	static {
		RENDER_TYPE_2_ENUM = Map.of(
				Sheets.cutoutItemSheet(), CUTOUT,
				Sheets.translucentItemSheet(), TRANSLUCENT,
				Sheets.cutoutBlockItemSheet(), CUTOUT_BLOCK,
				Sheets.translucentBlockItemSheet(), TRANSLUCENT_BLOCK
		);
	}
}
