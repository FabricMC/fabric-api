package net.fabricmc.fabric.impl.client.rendering.state;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.minecraft.class_11954;
import net.minecraft.client.render.entity.state.EntityRenderState;

import net.minecraft.client.render.item.ItemRenderState;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RenderStateDataTest {

	private static final RenderStateDataKey<String> DEBUG = RenderStateDataKey.create(() -> "Debug");

	@Test
	void assertFabricRenderStateMethods() {
		ItemRenderState itemRenderState = new ItemRenderState();
		FabricRenderState[] states = new FabricRenderState[]{
				new EntityRenderState(),
				new class_11954(),
				itemRenderState,
				itemRenderState.new LayerRenderState()
		};
		for (FabricRenderState state : states) {
			Assertions.assertNull(state.getData(DEBUG));
			state.setData(DEBUG, "test");
			Assertions.assertEquals("test", state.getData(DEBUG));
			state.clearData();
			Assertions.assertNull(state.getData(DEBUG));
		}
	}

}
