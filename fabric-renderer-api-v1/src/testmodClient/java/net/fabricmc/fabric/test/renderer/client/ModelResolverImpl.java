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

package net.fabricmc.fabric.test.renderer.client;

// TODO 1.21.4
//public class ModelResolverImpl implements ModelResolver {
//	private static final Set<Identifier> FRAME_MODEL_LOCATIONS = Set.of(
//			RendererTest.id("block/frame"),
//			RendererTest.id("item/frame"),
//			RendererTest.id("item/frame_multipart"),
//			RendererTest.id("item/frame_variant")
//	);
//
//	private static final Set<Identifier> PILLAR_MODEL_LOCATIONS = Set.of(
//			RendererTest.id("block/pillar"),
//			RendererTest.id("item/pillar")
//	);
//
//	private static final Set<Identifier> OCTAGONAL_COLUMN_MODEL_LOCATIONS = Set.of(
//			RendererTest.id("block/octagonal_column"),
//			RendererTest.id("item/octagonal_column")
//	);
//
//	private static final Set<Identifier> OCTAGONAL_COLUMN_VANILLA_MODEL_LOCATIONS = Set.of(
//			RendererTest.id("block/octagonal_column_vanilla")
//	);
//
//	private static final Set<Identifier> RIVERSTONE_MODEL_LOCATIONS = Set.of(
//			RendererTest.id("block/riverstone"),
//			RendererTest.id("item/riverstone")
//	);
//
//	@Override
//	@Nullable
//	public UnbakedModel resolveModel(Context context) {
//		Identifier id = context.id();
//
//		if (FRAME_MODEL_LOCATIONS.contains(id)) {
//			return new FrameUnbakedModel();
//		}
//
//		if (PILLAR_MODEL_LOCATIONS.contains(id)) {
//			return new PillarUnbakedModel();
//		}
//
//		if (OCTAGONAL_COLUMN_MODEL_LOCATIONS.contains(id)) {
//			return new OctagonalColumnUnbakedModel(ShadeMode.ENHANCED);
//		}
//
//		if (OCTAGONAL_COLUMN_VANILLA_MODEL_LOCATIONS.contains(id)) {
//			return new OctagonalColumnUnbakedModel(ShadeMode.VANILLA);
//		}
//
//		if (RIVERSTONE_MODEL_LOCATIONS.contains(id)) {
//			return new RiverstoneUnbakedModel();
//		}
//
//		return null;
//	}
//}
