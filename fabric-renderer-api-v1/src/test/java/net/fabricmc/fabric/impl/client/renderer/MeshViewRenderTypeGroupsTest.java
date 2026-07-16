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

package net.fabricmc.fabric.impl.client.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;

class MeshViewRenderTypeGroupsTest {
	@Test
	void splitFiltersIterationAndOutput() {
		QuadView solid = quad(false);
		QuadView translucent = quad(true);
		MeshViewRenderTypeGroups groups = MeshViewRenderTypeGroups.split(mesh(solid, translucent, solid));

		assertEquals(2, groups.solid().size());
		assertEquals(1, groups.translucent().size());
		assertEquals(List.of(solid, solid), collect(groups.solid()));
		assertEquals(List.of(translucent), collect(groups.translucent()));

		QuadEmitter emitter = mock(QuadEmitter.class);
		when(emitter.copyFrom(translucent)).thenReturn(emitter);
		when(emitter.emit()).thenReturn(emitter);
		groups.translucent().outputTo(emitter);

		verify(emitter).copyFrom(translucent);
		verify(emitter, never()).copyFrom(solid);
		verify(emitter).emit();
	}

	private static QuadView quad(boolean translucent) {
		RenderType renderType = mock(RenderType.class);
		when(renderType.hasBlending()).thenReturn(translucent);
		QuadView quad = mock(QuadView.class);
		when(quad.itemRenderType()).thenReturn(renderType);
		return quad;
	}

	private static MeshView mesh(QuadView... quads) {
		return new MeshView() {
			@Override
			public int size() {
				return quads.length;
			}

			@Override
			public void forEach(Consumer<? super QuadView> action) {
				for (QuadView quad : quads) {
					action.accept(quad);
				}
			}

			@Override
			public void outputTo(QuadEmitter emitter) {
				forEach(quad -> emitter.copyFrom(quad).emit());
			}
		};
	}

	private static List<QuadView> collect(MeshView mesh) {
		List<QuadView> quads = new ArrayList<>();
		mesh.forEach(quads::add);
		return quads;
	}
}
