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

package net.fabricmc.fabric.mixin.client.rendering;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
	@Accessor("renderHealthValue")
	int getRenderHealthValue();

	@Invoker("getRiddenEntity")
	LivingEntity callGetRiddenEntity();

	@Invoker("getHeartCount")
	int callGetHeartCount(LivingEntity entity);

	@Invoker("getHeartRows")
	int callGetHeartRows(int health);

	@Invoker("getCameraPlayer")
	PlayerEntity callGetCameraPlayer();
}
