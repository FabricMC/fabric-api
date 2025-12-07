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
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.impl.entity.event.effect.EffectEventContextImpl;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;

@Mixin(value = ContextChain.class, remap = false)
public final class ContextChainMixin {
	private ContextChainMixin() {
	}

	@WrapMethod(method = "runExecutable", remap = false)
	private static <S> int onRunExecutable(
			CommandContext<S> executable,
			S source,
			ResultConsumer<S> resultConsumer,
			boolean forkedMode,
			Operation<Integer> original
	) {
		int result;

		// if this isn't a LiteralCommandNode, we have bigger problems
		// since this is the first node
		if (!(executable.getNodes().getFirst().getNode() instanceof LiteralCommandNode<S> commandNode)) {
			return original.call(executable, source, resultConsumer, forkedMode);
		}

		try {
			MobEffectUtil.pushContext(new EffectEventContextImpl(
					true,
					commandNode.getName()
			));

			result = original.call(executable, source, resultConsumer, forkedMode);
		} finally {
			MobEffectUtil.popContext();
		}

		return result;
	}
}
