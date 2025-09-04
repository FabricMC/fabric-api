/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
