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

package net.fabricmc.fabric.impl.renderer;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;

/**
 * Indexes an atlas sprite to allow fast lookup of Sprites from
 * baked vertex coordinates.  Implementation is a straightforward
 * quad tree. Other options that were considered were linear search
 * (slow) and direct indexing of fixed-size cells. Direct indexing
 * would be fastest but would be memory-intensive for large atlases
 * and unsuitable for any atlas that isn't consistently aligned to
 * a fixed cell size.
 */
public class SpriteFinderImpl implements SpriteFinder {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpriteFinderImpl.class);

	private final Node root;
	private final Sprite missingSprite;
	private int badSpriteCount = 0;

	public SpriteFinderImpl(Map<Identifier, Sprite> sprites, Sprite missingSprite) {
		root = new Node(0.5f, 0.5f, 0.25f);
		this.missingSprite = missingSprite;
		sprites.values().forEach(root::add);
	}

	@Override
	public Sprite find(QuadView quad) {
		float u = 0;
		float v = 0;

		for (int i = 0; i < 4; i++) {
			u += quad.u(i);
			v += quad.v(i);
		}

		return find(u * 0.25f, v * 0.25f);
	}

	@Override
	public Sprite find(float u, float v) {
		return root.find(u, v);
	}

	private class Node {
		final float midU;
		final float midV;
		final float cellRadius;

		@Nullable
		Object lowLow = null;
		@Nullable
		Object lowHigh = null;
		@Nullable
		Object highLow = null;
		@Nullable
		Object highHigh = null;

		Node(float midU, float midV, float radius) {
			this.midU = midU;
			this.midV = midV;
			cellRadius = radius;
		}

		static final float EPS = 0.00001f;

		void add(Sprite sprite) {
			if (sprite.getMinU() < 0 - EPS || sprite.getMaxU() > 1 + EPS || sprite.getMinV() < 0 - EPS || sprite.getMaxV() > 1 + EPS) {
				// Sprite has broken bounds. This SHOULD NOT happen, but in the past some mods have broken this.
				// Prefer failing with a log warning rather than risking a stack overflow.
				if (badSpriteCount++ < 5) {
					String errorMessage = "SpriteFinderImpl: Skipping sprite {} with broken bounds [{}, {}]x[{}, {}]. Sprite bounds should be between 0 and 1.";
					LOGGER.error(errorMessage, sprite.getContents().getId(), sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
				}

				return;
			}

			final boolean lowU = sprite.getMinU() < midU - EPS;
			final boolean highU = sprite.getMaxU() > midU + EPS;
			final boolean lowV = sprite.getMinV() < midV - EPS;
			final boolean highV = sprite.getMaxV() > midV + EPS;

			if (lowU && lowV) {
				lowLow = addInner(sprite, lowLow, -1, -1);
			}

			if (lowU && highV) {
				lowHigh = addInner(sprite, lowHigh, -1, 1);
			}

			if (highU && lowV) {
				highLow = addInner(sprite, highLow, 1, -1);
			}

			if (highU && highV) {
				highHigh = addInner(sprite, highHigh, 1, 1);
			}
		}

		private Object addInner(Sprite sprite, @Nullable Object quadrant, int uStep, int vStep) {
			if (quadrant == null) {
				return sprite;
			} else if (quadrant instanceof Node node) {
				node.add(sprite);
				return quadrant;
			} else {
				Node n = new Node(midU + cellRadius * uStep, midV + cellRadius * vStep, cellRadius * 0.5f);

				if (quadrant instanceof Sprite prevSprite) {
					n.add(prevSprite);
				}

				n.add(sprite);
				return n;
			}
		}

		private Sprite find(float u, float v) {
			if (u < midU) {
				return v < midV ? findInner(lowLow, u, v) : findInner(lowHigh, u, v);
			} else {
				return v < midV ? findInner(highLow, u, v) : findInner(highHigh, u, v);
			}
		}

		private Sprite findInner(@Nullable Object quadrant, float u, float v) {
			if (quadrant instanceof Node node) {
				return node.find(u, v);
			} else if (quadrant instanceof Sprite sprite) {
				return sprite;
			} else {
				return missingSprite;
			}
		}
	}
}
