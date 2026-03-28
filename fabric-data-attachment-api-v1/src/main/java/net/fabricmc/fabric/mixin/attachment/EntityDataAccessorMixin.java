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

package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import net.fabricmc.fabric.impl.attachment.DataAccessorHandler;

@Mixin(EntityDataAccessor.class)
public abstract class EntityDataAccessorMixin implements DataAccessor {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("EntityDataAccessorMixin");

	@Shadow
	@Final
	private Entity entity;

	@WrapMethod(method = "setData")
	public void setData(CompoundTag tag, Operation<Void> original) {
		if (entity.level() == null) {
			// The block entity is not in a level, just follow the default logic.
			original.call(tag);
			return;
		}

		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
			ValueInput data = TagValueInput.create(reporter, this.entity.level().registryAccess(), tag);
			DataAccessorHandler.applyDataChanges(this.entity, data, () -> original.call(tag));
		}
	}
}
