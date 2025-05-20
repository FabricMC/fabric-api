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

package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

/**
 * Collection of utilities for model implementations.
 */
public final class ModelHelper {
	/** @see #faceFromIndex(int) */
	private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);

	/** Result from {@link #toFaceIndex(Direction)} for null values. */
	public static final int NULL_FACE_ID = 6;

	private ModelHelper() { }

	/**
	 * Convenient way to encode faces that may be null.
	 * Null is returned as {@link #NULL_FACE_ID}.
	 * Use {@link #faceFromIndex(int)} to retrieve encoded face.
	 */
	public static int toFaceIndex(@Nullable Direction face) {
		return face == null ? NULL_FACE_ID : face.getIndex();
	}

	/**
	 * Use to decode a result from {@link #toFaceIndex(Direction)}.
	 * Return value will be null if encoded value was null.
	 * Can also be used for no-allocation iteration of {@link Direction#values()},
	 * optionally including the null face. (Use &lt; or  &lt;= {@link #NULL_FACE_ID}
	 * to exclude or include the null value, respectively.)
	 */
	@Nullable
	public static Direction faceFromIndex(int faceIndex) {
		return FACES[faceIndex];
	}

	/**
	 * Converts a mesh into an array of lists of vanilla baked quads.
	 * Useful for creating vanilla baked models when required for compatibility.
	 * The array indexes correspond to {@link Direction#getId()} with the
	 * addition of {@link #NULL_FACE_ID}.
	 *
	 * <p>Retrieves sprites from the block texture atlas via {@link SpriteFinder}.
	 */
	public static List<BakedQuad>[] toQuadLists(Mesh mesh) {
		SpriteFinder finder = MinecraftClient.getInstance().getBakedModelManager().getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).spriteFinder();

		@SuppressWarnings("unchecked")
		final ImmutableList.Builder<BakedQuad>[] builders = new ImmutableList.Builder[7];

		for (int i = 0; i < 7; i++) {
			builders[i] = ImmutableList.builder();
		}

		mesh.forEach(q -> {
			Direction cullFace = q.cullFace();
			builders[cullFace == null ? NULL_FACE_ID : cullFace.getIndex()].add(q.toBakedQuad(finder.find(q)));
		});

		@SuppressWarnings("unchecked")
		List<BakedQuad>[] result = new List[7];

		for (int i = 0; i < 7; i++) {
			result[i] = builders[i].build();
		}

		return result;
	}
}
