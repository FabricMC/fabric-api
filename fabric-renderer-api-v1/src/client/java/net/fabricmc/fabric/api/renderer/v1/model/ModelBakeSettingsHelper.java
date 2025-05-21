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

import java.util.EnumMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.AffineTransformations;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;

/**
 * Utilities to make it easier to work with {@link ModelBakeSettings}.
 */
public final class ModelBakeSettingsHelper {
	private static final Direction[] DIRECTIONS = Direction.values();

	private ModelBakeSettingsHelper() {
	}

	/**
	 * Creates a new {@link ModelBakeSettings} using the given transformation and enables UV lock if specified. Works
	 * exactly like {@link ModelRotation}, but allows an arbitrary transformation. Instances should be retained and
	 * reused, especially if UV lock is enabled, to avoid redoing costly computations.
	 */
	public static ModelBakeSettings of(AffineTransformation transformation, boolean uvLock) {
		Matrix4fc matrix = transformation.getMatrix();

		if (MatrixUtil.isIdentity(matrix)) {
			return ModelRotation.X0_Y0;
		}

		if (!uvLock) {
			return new ModelBakeSettings() {
				@Override
				public AffineTransformation getRotation() {
					return transformation;
				}
			};
		}

		Map<Direction, Matrix4fc> faceTransformations = new EnumMap<>(Direction.class);
		Map<Direction, Matrix4fc> inverseFaceTransformations = new EnumMap<>(Direction.class);

		for (Direction face : DIRECTIONS) {
			Matrix4fc faceTransformation = AffineTransformations.getTransformed(transformation, face).getMatrix();
			faceTransformations.put(face, faceTransformation);
			inverseFaceTransformations.put(face, faceTransformation.invert(new Matrix4f()));
		}

		return new ModelBakeSettings() {
			@Override
			public AffineTransformation getRotation() {
				return transformation;
			}

			@Override
			public Matrix4fc forward(Direction face) {
				return faceTransformations.get(face);
			}

			@Override
			public Matrix4fc reverse(Direction face) {
				return inverseFaceTransformations.get(face);
			}
		};
	}

	/**
	 * Creates a new {@link ModelBakeSettings} that is the product of the two given settings. Settings are represented
	 * by matrices, so this method follows the rules of matrix multiplication, namely that applying the resulting
	 * settings is (mostly) equivalent to applying the right settings and then the left settings. The only exception
	 * during standard application is cull face transformation, as the result must be clamped. Thus, applying a single
	 * premultiplied transformation generally yields better results than multiple applications.
	 */
	public static ModelBakeSettings multiply(ModelBakeSettings left, ModelBakeSettings right) {
		// Assumes face transformations are identity if main transformation is identity
		if (MatrixUtil.isIdentity(left.getRotation().getMatrix())) {
			return right;
		} else if (MatrixUtil.isIdentity(right.getRotation().getMatrix())) {
			return left;
		}

		AffineTransformation transformation = left.getRotation().multiply(right.getRotation());

		boolean leftHasFaceTransformations = false;
		boolean rightHasFaceTransformations = false;

		// Assumes inverse face transformations are exactly inverse of regular face transformations
		for (Direction face : DIRECTIONS) {
			if (!leftHasFaceTransformations && !MatrixUtil.isIdentity(left.forward(face))) {
				leftHasFaceTransformations = true;
			}

			if (!rightHasFaceTransformations && !MatrixUtil.isIdentity(right.forward(face))) {
				rightHasFaceTransformations = true;
			}
		}

		if (leftHasFaceTransformations & rightHasFaceTransformations) {
			Map<Direction, Matrix4fc> faceTransformations = new EnumMap<>(Direction.class);
			Map<Direction, Matrix4fc> inverseFaceTransformations = new EnumMap<>(Direction.class);

			for (Direction face : DIRECTIONS) {
				faceTransformations.put(face, left.forward(face).mul(right.forward(face), new Matrix4f()));
				inverseFaceTransformations.put(face, right.reverse(face).mul(left.reverse(face), new Matrix4f()));
			}

			return new ModelBakeSettings() {
				@Override
				public AffineTransformation getRotation() {
					return transformation;
				}

				@Override
				public Matrix4fc forward(Direction face) {
					return faceTransformations.get(face);
				}

				@Override
				public Matrix4fc reverse(Direction face) {
					return inverseFaceTransformations.get(face);
				}
			};
		}

		ModelBakeSettings faceTransformDelegate = leftHasFaceTransformations ? left : right;

		return new ModelBakeSettings() {
			@Override
			public AffineTransformation getRotation() {
				return transformation;
			}

			@Override
			public Matrix4fc forward(Direction face) {
				return faceTransformDelegate.forward(face);
			}

			@Override
			public Matrix4fc reverse(Direction face) {
				return faceTransformDelegate.reverse(face);
			}
		};
	}

	/**
	 * Creates a new {@link QuadTransform} that applies the given transformation. The sprite finder is used to look up
	 * the current sprite to correctly apply UV lock, if present in the transformation.
	 *
	 * <p>This method is most useful when creating custom implementations of {@link Geometry}, which receive a
	 * {@link ModelBakeSettings}.
	 */
	public static QuadTransform asQuadTransform(ModelBakeSettings settings, SpriteFinder spriteFinder) {
		Matrix4fc matrix = settings.getRotation().getMatrix();

		// Assumes face transformations are identity if main transformation is identity
		if (MatrixUtil.isIdentity(matrix)) {
			return q -> true;
		}

		Matrix3f normalMatrix = matrix.normal(new Matrix3f());

		Vector4f vec4 = new Vector4f();
		Vector3f vec3 = new Vector3f();

		return quad -> {
			Direction lightFace = quad.lightFace();
			Matrix4fc reverseMatrix = settings.reverse(lightFace);

			if (!MatrixUtil.isIdentity(reverseMatrix)) {
				Sprite sprite = spriteFinder.find(quad);

				for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
					float frameU = sprite.getFrameFromU(quad.u(vertexIndex));
					float frameV = sprite.getFrameFromV(quad.v(vertexIndex));
					vec3.set(frameU - 0.5f, frameV - 0.5f, 0.0f);
					reverseMatrix.transformPosition(vec3);
					frameU = vec3.x + 0.5f;
					frameV = vec3.y + 0.5f;
					quad.uv(vertexIndex, sprite.getFrameU(frameU), sprite.getFrameV(frameV));
				}
			}

			for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
				vec4.set(quad.x(vertexIndex) - 0.5f, quad.y(vertexIndex) - 0.5f, quad.z(vertexIndex) - 0.5f, 1.0f);
				vec4.mul(matrix);
				quad.pos(vertexIndex, vec4.x + 0.5f, vec4.y + 0.5f, vec4.z + 0.5f);

				if (quad.hasNormal(vertexIndex)) {
					quad.copyNormal(vertexIndex, vec3);
					vec3.mul(normalMatrix);
					vec3.normalize();
					quad.normal(vertexIndex, vec3);
				}
			}

			Direction cullFace = quad.cullFace();

			if (cullFace != null) {
				quad.cullFace(Direction.transform(matrix, cullFace));
			}

			return true;
		};
	}
}
