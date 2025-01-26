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

package net.fabricmc.fabric.mixin.client.gametest.screenshot;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.texture.NativeImage;

import net.fabricmc.fabric.impl.client.gametest.screenshot.NativeImageHooks;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin implements NativeImageHooks {
	@Shadow
	private long pointer;
	@Shadow
	@Final
	private NativeImage.Format format;

	@Shadow
	protected abstract void checkAllocated();
	@Shadow
	public abstract int getWidth();
	@Shadow
	public abstract int getHeight();
	@Shadow
	public abstract int[] copyPixelsAbgr();

	@Override
	public byte[] fabric_copyPixelsLuminance() {
		this.checkAllocated();
		byte[] result = new byte[getWidth() * getHeight()];

		switch (this.format) {
		case RGBA -> {
			for (int i = 0; i < result.length; i++) {
				int red = MemoryUtil.memGetByte(pointer + i * 4) & 0xff;
				int green = MemoryUtil.memGetByte(pointer + i * 4 + 1) & 0xff;
				int blue = MemoryUtil.memGetByte(pointer + i * 4 + 2) & 0xff;
				result[i] = toGrayscale(red, green, blue);
			}
		}
		case RGB -> {
			for (int i = 0; i < result.length; i++) {
				int red = MemoryUtil.memGetByte(pointer + i * 3) & 0xff;
				int green = MemoryUtil.memGetByte(pointer + i * 3 + 1) & 0xff;
				int blue = MemoryUtil.memGetByte(pointer + i * 3 + 2) & 0xff;
				result[i] = toGrayscale(red, green, blue);
			}
		}
		case LUMINANCE_ALPHA -> {
			for (int i = 0; i < result.length; i++) {
				result[i] = MemoryUtil.memGetByte(pointer + i * 2);
			}
		}
		case LUMINANCE -> MemoryUtil.memByteBuffer(pointer, getWidth() * getHeight()).get(result);
		}

		return result;
	}

	@Override
	public int[] fabric_copyPixelsRgb() {
		this.checkAllocated();

		return switch (this.format) {
		case RGBA -> {
			int[] result = this.copyPixelsAbgr();

			for (int i = 0; i < result.length; i++) {
				int color = result[i];
				int blue = (color >> 16) & 0xff;
				int green = (color >> 8) & 0xff;
				int red = color & 0xff;
				result[i] = (red << 16) | (green << 8) | blue;
			}

			yield result;
		}
		case RGB -> {
			int[] result = new int[getWidth() * getHeight()];

			for (int i = 0; i < result.length; i++) {
				int red = MemoryUtil.memGetByte(pointer + i * 3) & 0xff;
				int green = MemoryUtil.memGetByte(pointer + i * 3 + 1) & 0xff;
				int blue = MemoryUtil.memGetByte(pointer + i * 3 + 2) & 0xff;
				result[i] = (red << 16) | (green << 8) | blue;
			}

			yield result;
		}
		case LUMINANCE_ALPHA -> {
			int[] result = new int[getWidth() * getHeight()];

			for (int i = 0; i < result.length; i++) {
				int luminance = MemoryUtil.memGetByte(pointer + i * 2) & 0xff;
				result[i] = (luminance << 16) | (luminance << 8) | luminance;
			}

			yield result;
		}
		case LUMINANCE -> {
			int[] result = new int[getWidth() * getHeight()];

			for (int i = 0; i < result.length; i++) {
				int luminance = MemoryUtil.memGetByte(pointer + i) & 0xff;
				result[i] = (luminance << 16) | (luminance << 8) | luminance;
			}

			yield result;
		}
		};
	}

	@Override
	public boolean fabric_isFullyOpaque() {
		if (!format.hasAlpha()) {
			return true;
		}

		int size = getWidth() * getHeight();
		int alphaOffset = format.getAlphaOffset() / 8;

		for (int i = 0; i < size; i++) {
			int alpha = MemoryUtil.memGetByte(pointer + i * format.getChannelCount() + alphaOffset) & 0xff;

			if (alpha != 255) {
				return false;
			}
		}

		return true;
	}

	@Unique
	private static byte toGrayscale(int red, int green, int blue) {
		// https://github.com/nothings/stb/blob/5c205738c191bcb0abc65c4febfa9bd25ff35234/stb_image.h#L1748
		return (byte) (((red * 77) + (green * 150) + (blue * 29)) >> 8);
	}
}
