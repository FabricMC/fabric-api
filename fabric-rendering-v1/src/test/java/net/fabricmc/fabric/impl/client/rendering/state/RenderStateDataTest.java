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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.client.render.state.WorldRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

public class RenderStateDataTest {
	private static final RenderStateDataKey<String> DEBUG = RenderStateDataKey.create(() -> "Debug");

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void assertFabricRenderStateMethods() {
		ItemRenderState itemRenderState = new ItemRenderState();
		FabricRenderState[] states = new FabricRenderState[]{
				new EntityRenderState(),
				new BlockEntityRenderState(),
				itemRenderState,
				itemRenderState.new LayerRenderState(),
				new MapRenderState(),
				new MapRenderState.Decoration(),
				new MovingBlockRenderState(),
				new WorldRenderState(),
				new CameraRenderState(),
				new WeatherRenderState(),
				new WorldBorderRenderState(),
				new SkyRenderState()
		};

		for (FabricRenderState state : states) {
			Assertions.assertNull(state.getData(DEBUG));
			Assertions.assertEquals("pass", state.getDataOrDefault(DEBUG, "pass"));
			state.setData(DEBUG, "test");
			Assertions.assertEquals("test", state.getData(DEBUG));
			Assertions.assertEquals("test", state.getDataOrDefault(DEBUG, "fail"));
			state.clearExtraData();
			Assertions.assertNull(state.getData(DEBUG));
			Assertions.assertEquals("pass", state.getDataOrDefault(DEBUG, "pass"));
		}
	}
}
