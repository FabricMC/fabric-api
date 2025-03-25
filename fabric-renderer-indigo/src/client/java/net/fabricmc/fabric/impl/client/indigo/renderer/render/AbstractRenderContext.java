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

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;

public abstract class AbstractRenderContext {
	private final MutableQuadViewImpl editorQuad = new MutableQuadViewImpl() {
		{
			data = new int[EncodingFormat.TOTAL_STRIDE];
			clear();
		}

		@Override
		protected void emitDirectly() {
			bufferQuad(this);
		}
	};

	private final Vector4f posVec = new Vector4f();
	private final Vector3f normalVec = new Vector3f();

	protected MatrixStack.Entry matrices;
	protected int overlay;

	protected QuadEmitter getEmitter() {
		editorQuad.clear();
		return editorQuad;
	}

	protected abstract void bufferQuad(MutableQuadViewImpl quad);

	/** final output step, common to all renders. */
	protected void bufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer) {
		final Vector4f posVec = this.posVec;
		final Vector3f normalVec = this.normalVec;
		final MatrixStack.Entry matrices = this.matrices;
		final Matrix4f posMatrix = matrices.getPositionMatrix();
		final boolean useNormals = quad.hasVertexNormals();

		if (useNormals) {
			quad.populateMissingNormals();
		} else {
			matrices.transformNormal(quad.faceNormal(), normalVec);
		}

		for (int i = 0; i < 4; i++) {
			posVec.set(quad.x(i), quad.y(i), quad.z(i), 1.0f);
			posVec.mul(posMatrix);

			if (useNormals) {
				quad.copyNormal(i, normalVec);
				matrices.transformNormal(normalVec, normalVec);
			}

			vertexConsumer.vertex(posVec.x(), posVec.y(), posVec.z(), quad.color(i), quad.u(i), quad.v(i), overlay, quad.lightmap(i), normalVec.x(), normalVec.y(), normalVec.z());
		}
	}
}
