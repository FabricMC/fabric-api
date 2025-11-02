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

package net.fabricmc.fabric.test.rendering.client;

import org.joml.Matrix4f;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class TestRenderUtils {
	public static void drawFilledBox(MatrixStack matrices, VertexConsumer vertexConsumers, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		// Front
		vertexConsumers.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
		// Back
		vertexConsumers.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
		// Left
		vertexConsumers.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
		// Right
		vertexConsumers.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
		// Top
		vertexConsumers.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha);
		// Bottom
		vertexConsumers.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha);
	}
}
