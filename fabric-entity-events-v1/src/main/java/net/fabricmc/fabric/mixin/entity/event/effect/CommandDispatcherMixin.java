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

package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;

@Mixin(value = CommandDispatcher.class, remap = false)
public final class CommandDispatcherMixin<S> {
	private CommandDispatcherMixin() {
	}

	@WrapMethod(method = "execute(Lcom/mojang/brigadier/ParseResults;)I")
	private int onExecute(ParseResults<S> parse, Operation<Integer> original) {
		MobEffectUtil.ARE_WE_COMMAND.set(true);
		int result = original.call(parse);
		MobEffectUtil.ARE_WE_COMMAND.set(false);
		return result;
	}
}
