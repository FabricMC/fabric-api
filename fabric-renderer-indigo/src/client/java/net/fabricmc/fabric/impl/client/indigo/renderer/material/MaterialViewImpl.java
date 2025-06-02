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

package net.fabricmc.fabric.impl.client.indigo.renderer.material;

import static net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat.bitMask;

import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.fabric.api.renderer.v1.material.MaterialView;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;

/**
 * Default implementation of the standard render materials.
 * The underlying representation is simply an int with bit-wise
 * packing of the various material properties. This offers
 * easy/fast interning via int/object hashmap.
 */
public class MaterialViewImpl implements MaterialView {
	private static final @Nullable BlockRenderLayer[] BLOCK_RENDER_LAYERS = ArrayUtils.add(BlockRenderLayer.values(), null);
	private static final int BLOCK_RENDER_LAYER_COUNT = BLOCK_RENDER_LAYERS.length;
	private static final TriState[] TRI_STATES = TriState.values();
	private static final int TRI_STATE_COUNT = TRI_STATES.length;
	private static final @Nullable ItemRenderState.Glint[] GLINTS = ArrayUtils.add(ItemRenderState.Glint.values(), null);
	private static final int GLINT_COUNT = GLINTS.length;
	private static final ShadeMode[] SHADE_MODES = ShadeMode.values();
	private static final int SHADE_MODE_COUNT = SHADE_MODES.length;

	protected static final int NULL_RENDER_LAYER_INDEX = BLOCK_RENDER_LAYER_COUNT - 1;
	protected static final int NULL_GLINT_INDEX = GLINT_COUNT - 1;

	protected static final int RENDER_LAYER_BIT_LENGTH = MathHelper.ceilLog2(BLOCK_RENDER_LAYER_COUNT);
	protected static final int EMISSIVE_BIT_LENGTH = 1;
	protected static final int DIFFUSE_BIT_LENGTH = 1;
	protected static final int AO_BIT_LENGTH = MathHelper.ceilLog2(TRI_STATE_COUNT);
	protected static final int GLINT_BIT_LENGTH = MathHelper.ceilLog2(GLINT_COUNT);
	protected static final int SHADE_MODE_BIT_LENGTH = MathHelper.ceilLog2(SHADE_MODE_COUNT);

	protected static final int RENDER_LAYER_BIT_OFFSET = 0;
	protected static final int EMISSIVE_BIT_OFFSET = RENDER_LAYER_BIT_OFFSET + RENDER_LAYER_BIT_LENGTH;
	protected static final int DIFFUSE_BIT_OFFSET = EMISSIVE_BIT_OFFSET + EMISSIVE_BIT_LENGTH;
	protected static final int AO_BIT_OFFSET = DIFFUSE_BIT_OFFSET + DIFFUSE_BIT_LENGTH;
	protected static final int GLINT_BIT_OFFSET = AO_BIT_OFFSET + AO_BIT_LENGTH;
	protected static final int SHADE_MODE_BIT_OFFSET = GLINT_BIT_OFFSET + GLINT_BIT_LENGTH;
	public static final int TOTAL_BIT_LENGTH = SHADE_MODE_BIT_OFFSET + SHADE_MODE_BIT_LENGTH;

	protected static final int RENDER_LAYER_MASK = bitMask(RENDER_LAYER_BIT_LENGTH, RENDER_LAYER_BIT_OFFSET);
	protected static final int EMISSIVE_FLAG = bitMask(EMISSIVE_BIT_LENGTH, EMISSIVE_BIT_OFFSET);
	protected static final int DIFFUSE_FLAG = bitMask(DIFFUSE_BIT_LENGTH, DIFFUSE_BIT_OFFSET);
	protected static final int AO_MASK = bitMask(AO_BIT_LENGTH, AO_BIT_OFFSET);
	protected static final int GLINT_MASK = bitMask(GLINT_BIT_LENGTH, GLINT_BIT_OFFSET);
	protected static final int SHADE_MODE_MASK = bitMask(SHADE_MODE_BIT_LENGTH, SHADE_MODE_BIT_OFFSET);

	protected static boolean areBitsValid(int bits) {
		int renderLayer = (bits & RENDER_LAYER_MASK) >>> RENDER_LAYER_BIT_OFFSET;
		int ao = (bits & AO_MASK) >>> AO_BIT_OFFSET;
		int glint = (bits & GLINT_MASK) >>> GLINT_BIT_OFFSET;
		int shadeMode = (bits & SHADE_MODE_MASK) >>> SHADE_MODE_BIT_OFFSET;

		return renderLayer < BLOCK_RENDER_LAYER_COUNT
				&& ao < TRI_STATE_COUNT
				&& glint < GLINT_COUNT
				&& shadeMode < SHADE_MODE_COUNT;
	}

	protected int bits;

	protected MaterialViewImpl(int bits) {
		this.bits = bits;
	}

	@Override
	@Nullable
	public BlockRenderLayer renderLayer() {
		return BLOCK_RENDER_LAYERS[(bits & RENDER_LAYER_MASK) >>> RENDER_LAYER_BIT_OFFSET];
	}

	@Override
	public boolean emissive() {
		return (bits & EMISSIVE_FLAG) != 0;
	}

	@Override
	public boolean disableDiffuse() {
		return (bits & DIFFUSE_FLAG) != 0;
	}

	@Override
	public TriState ambientOcclusion() {
		return TRI_STATES[(bits & AO_MASK) >>> AO_BIT_OFFSET];
	}

	@Override
	@Nullable
	public ItemRenderState.Glint glint() {
		return GLINTS[(bits & GLINT_MASK) >>> GLINT_BIT_OFFSET];
	}

	@Override
	public ShadeMode shadeMode() {
		return SHADE_MODES[(bits & SHADE_MODE_MASK) >>> SHADE_MODE_BIT_OFFSET];
	}

	/**
	 * Returns a string representation of the material properties.
	 * To be used in {@link #toString} overrides so they show in the debugger.
	 */
	String contentsToString() {
		return String.format("render type=%s, emissive=%b, disable diffuse=%b, ao=%s, glint=%s, shade=%s",
				String.valueOf(renderLayer()).toLowerCase(Locale.ROOT),
				emissive(),
				disableDiffuse(),
				String.valueOf(ambientOcclusion()).toLowerCase(Locale.ROOT),
				String.valueOf(glint()).toLowerCase(Locale.ROOT),
				String.valueOf(shadeMode()).toLowerCase(Locale.ROOT));
	}
}
