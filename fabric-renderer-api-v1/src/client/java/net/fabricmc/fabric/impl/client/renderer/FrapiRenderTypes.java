package net.fabricmc.fabric.impl.client.renderer;

import java.util.List;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public final class FrapiRenderTypes {
	private FrapiRenderTypes() {
	}

	public static RenderType getMovingBlockRenderType(ChunkSectionLayer layer) {
		return switch (layer) {
		case SOLID -> RenderTypes.solidMovingBlock();
		case CUTOUT -> RenderTypes.cutoutMovingBlock();
		case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
		};
	}

	public static RenderType getRenderType(ChunkSectionLayer layer) {
		return switch (layer) {
		case SOLID, CUTOUT -> Sheets.cutoutBlockSheet();
		case TRANSLUCENT -> Sheets.translucentBlockSheet();
		};
	}

	public static RenderType getBlockModelRenderType(final List<BlockStateModelPart> parts) {
		boolean translucent = false;

		for (BlockStateModelPart part : parts) {
			translucent |= part.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT);
		}

		return translucent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();
	}

	public static RenderType getBlockModelRenderType(final BlockStateModel model) {
		return model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();
	}
}
