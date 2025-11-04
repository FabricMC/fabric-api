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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.world.phys.AABB;

public class TestRenderUtils {
	public static void drawFilledBox(PoseStack matrices, VertexConsumer vertexConsumers, AABB box, int color) {
		Matrix4f matrix4f = matrices.last().pose();

		// Front
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(color);
		// Back
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(color);
		// Left
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(color);
		// Right
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(color);
		// Top
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(color);
		// Bottom
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(color);
		vertexConsumers.addVertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(color);
	}
}
