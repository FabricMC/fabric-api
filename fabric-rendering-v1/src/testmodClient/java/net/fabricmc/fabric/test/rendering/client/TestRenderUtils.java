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
import net.minecraft.util.math.Box;

public class TestRenderUtils {
	public static void drawFilledBox(MatrixStack matrices, VertexConsumer vertexConsumers, Box box, int color) {
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		float red = (float) (color >> 16 & 0xFF) / 255.0F;
		float green = (float) (color >> 8 & 0xFF) / 255.0F;
		float blue = (float) (color & 0xFF) / 255.0F;
		float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

		// Front
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
		// Back
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
		// Left
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
		// Right
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
		// Top
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
		// Bottom
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
		vertexConsumers.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
	}
}
