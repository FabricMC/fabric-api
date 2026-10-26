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

package net.fabricmc.fabric.test.block.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import net.fabricmc.fabric.api.block.v1.FabricBlock;

@Mixin(ChiseledBookShelfBlock.class)
class ChiseledBookShelfBlockMixin implements FabricBlock {
	@Shadow
	@Final
	public static List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES;

	@Override
	public float getProvidedEnchantmentPower(BlockState state, BlockGetter level, BlockPos pos) {
		float power = 0;

		for (BooleanProperty SLOT_OCCUPIED_PROPERTY : SLOT_OCCUPIED_PROPERTIES) {
			if (state.getValue(SLOT_OCCUPIED_PROPERTY)) {
				power++;
			}
		}

		return power == 0 ? -15 : power;
	}
}
