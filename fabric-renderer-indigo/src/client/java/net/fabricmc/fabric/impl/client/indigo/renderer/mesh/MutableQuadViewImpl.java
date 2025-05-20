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

package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.HEADER_BITS;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.HEADER_STRIDE;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.HEADER_TAG;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.HEADER_TINT_INDEX;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.VERTEX_COLOR;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.VERTEX_LIGHTMAP;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.VERTEX_NORMAL;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.VERTEX_STRIDE;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.VERTEX_U;
import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.VERTEX_X;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.NormalHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.material.RenderMaterialImpl;

/**
 * Almost-concrete implementation of a mutable quad. The only missing part is {@link #emitDirectly()},
 * because that depends on where/how it is used. (Mesh encoding vs. render-time transformation).
 *
 * <p>In many cases an instance of this class is used as an "editor quad". The editor quad's
 * {@link #emitDirectly()} method calls some other internal method that transforms the quad
 * data and then buffers it. Transformations should be the same as they would be in a vanilla
 * render - the editor is serving mainly as a way to access vertex data without magical
 * numbers. It also allows for a consistent interface for those transformations.
 */
public abstract class MutableQuadViewImpl extends QuadViewImpl implements QuadEmitter {
	private static final QuadTransform NO_TRANSFORM = q -> true;

	private static final int[] DEFAULT_QUAD_DATA = new int[EncodingFormat.TOTAL_STRIDE];

	static {
		MutableQuadViewImpl quad = new MutableQuadViewImpl() {
			@Override
			protected void emitDirectly() {
				// This quad won't be emitted. It's only used to configure the default quad data.
			}
		};

		// Start with all zeroes
		quad.data = DEFAULT_QUAD_DATA;
		// Apply non-zero defaults
		quad.color(-1, -1, -1, -1);
		quad.cullFace(null);
		quad.material(IndigoRenderer.STANDARD_MATERIAL);
		quad.tintIndex(-1);
	}

	private QuadTransform activeTransform = NO_TRANSFORM;
	private final ObjectArrayList<QuadTransform> transformStack = new ObjectArrayList<>();
	private final QuadTransform stackTransform = q -> {
		int i = transformStack.size() - 1;

		while (i >= 0) {
			if (!transformStack.get(i--).transform(q)) {
				return false;
			}
		}

		return true;
	};

	public final void clear() {
		System.arraycopy(DEFAULT_QUAD_DATA, 0, data, baseIndex, EncodingFormat.TOTAL_STRIDE);
		isGeometryInvalid = true;
		nominalFace = null;
	}

	@Override
	public final MutableQuadViewImpl pos(int vertexIndex, float x, float y, float z) {
		final int index = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X;
		data[index] = Float.floatToRawIntBits(x);
		data[index + 1] = Float.floatToRawIntBits(y);
		data[index + 2] = Float.floatToRawIntBits(z);
		isGeometryInvalid = true;
		return this;
	}

	@Override
	public final MutableQuadViewImpl color(int vertexIndex, int color) {
		data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_COLOR] = color;
		return this;
	}

	@Override
	public final MutableQuadViewImpl uv(int vertexIndex, float u, float v) {
		final int i = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_U;
		data[i] = Float.floatToRawIntBits(u);
		data[i + 1] = Float.floatToRawIntBits(v);
		return this;
	}

	@Override
	public final MutableQuadViewImpl lightmap(int vertexIndex, int lightmap) {
		data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP] = lightmap;
		return this;
	}

	protected final void normalFlags(int flags) {
		data[baseIndex + HEADER_BITS] = EncodingFormat.normalFlags(data[baseIndex + HEADER_BITS], flags);
	}

	@Override
	public final MutableQuadViewImpl normal(int vertexIndex, float x, float y, float z) {
		normalFlags(normalFlags() | (1 << vertexIndex));
		data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_NORMAL] = NormalHelper.packNormal(x, y, z);
		return this;
	}

	/**
	 * Internal helper method. Copies face normals to vertex normals lacking one.
	 */
	public final void populateMissingNormals() {
		final int normalFlags = this.normalFlags();

		if (normalFlags == 0b1111) return;

		final int packedFaceNormal = packedFaceNormal();

		for (int v = 0; v < 4; v++) {
			if ((normalFlags & (1 << v)) == 0) {
				data[baseIndex + v * VERTEX_STRIDE + VERTEX_NORMAL] = packedFaceNormal;
			}
		}

		normalFlags(0b1111);
	}

	@Override
	public final MutableQuadViewImpl cullFace(@Nullable Direction face) {
		data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(data[baseIndex + HEADER_BITS], face);
		nominalFace(face);
		return this;
	}

	@Override
	public final MutableQuadViewImpl nominalFace(@Nullable Direction face) {
		nominalFace = face;
		return this;
	}

	@Override
	public final MutableQuadViewImpl material(RenderMaterial material) {
		data[baseIndex + HEADER_BITS] = EncodingFormat.material(data[baseIndex + HEADER_BITS], (RenderMaterialImpl) material);
		return this;
	}

	@Override
	public final MutableQuadViewImpl tintIndex(int tintIndex) {
		data[baseIndex + HEADER_TINT_INDEX] = tintIndex;
		return this;
	}

	@Override
	public final MutableQuadViewImpl tag(int tag) {
		data[baseIndex + HEADER_TAG] = tag;
		return this;
	}

	@Override
	public final MutableQuadViewImpl copyFrom(QuadView quad) {
		final QuadViewImpl q = (QuadViewImpl) quad;
		System.arraycopy(q.data, q.baseIndex, data, baseIndex, EncodingFormat.TOTAL_STRIDE);
		nominalFace = q.nominalFace;
		isGeometryInvalid = q.isGeometryInvalid;

		if (!isGeometryInvalid) {
			faceNormal.set(q.faceNormal);
		}

		return this;
	}

	@Override
	public final MutableQuadViewImpl fromVanilla(int[] quadData, int startIndex) {
		System.arraycopy(quadData, startIndex, data, baseIndex + HEADER_STRIDE, VANILLA_QUAD_STRIDE);
		isGeometryInvalid = true;

		int normalFlags = 0;
		int colorIndex = baseIndex + VERTEX_COLOR;
		int normalIndex = baseIndex + VERTEX_NORMAL;

		for (int i = 0; i < 4; i++) {
			data[colorIndex] = ColorHelper.fromVanillaColor(data[colorIndex]);

			// Set normal flag if normal is not zero, ignoring W component
			if ((data[normalIndex] & 0xFFFFFF) != 0) {
				normalFlags |= 1 << i;
			}

			colorIndex += VERTEX_STRIDE;
			normalIndex += VERTEX_STRIDE;
		}

		normalFlags(normalFlags);
		return this;
	}

	@Override
	public final MutableQuadViewImpl fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
		fromVanilla(quad.vertexData(), 0);
		cullFace(cullFace);
		nominalFace(quad.face());
		tintIndex(quad.tintIndex());

		if (!quad.shade()) {
			material = RenderMaterialImpl.setDisableDiffuse((RenderMaterialImpl) material, true);
		}

		int lightEmission = quad.lightEmission();

		if (lightEmission > 0) {
			for (int i = 0; i < 4; i++) {
				lightmap(i, LightmapTextureManager.applyEmission(lightmap(i), lightEmission));
			}
		}

		material(material);
		tag(0);
		return this;
	}

	@Override
	public void pushTransform(QuadTransform transform) {
		if (transform == null) {
			throw new NullPointerException("QuadTransform cannot be null!");
		}

		transformStack.push(transform);

		if (transformStack.size() == 1) {
			activeTransform = transform;
		} else if (transformStack.size() == 2) {
			activeTransform = stackTransform;
		}
	}

	@Override
	public void popTransform() {
		transformStack.pop();

		if (transformStack.size() == 0) {
			activeTransform = NO_TRANSFORM;
		} else if (transformStack.size() == 1) {
			activeTransform = transformStack.get(0);
		}
	}

	/**
	 * Emit the quad without applying transforms and without clearing the underlying data.
	 * Geometry is not guaranteed to be valid when called, but can be computed by calling {@link #computeGeometry()}.
	 */
	protected abstract void emitDirectly();

	/**
	 * Apply transforms and then if transforms return true, emit the quad without clearing the underlying data.
	 */
	public final void transformAndEmit() {
		if (activeTransform.transform(this)) {
			emitDirectly();
		}
	}

	@Override
	public final MutableQuadViewImpl emit() {
		transformAndEmit();
		clear();
		return this;
	}
}
