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
			MobEffectUtil.CURRENT_COMMAND_CONTEXT.get().push(new EffectEventContextImpl(
					true,
					commandNode.getName()
			));

			result = original.call(executable, source, resultConsumer, forkedMode);
		} finally {
			MobEffectUtil.CURRENT_COMMAND_CONTEXT.get().pop();
		}

		return result;
	}
}
