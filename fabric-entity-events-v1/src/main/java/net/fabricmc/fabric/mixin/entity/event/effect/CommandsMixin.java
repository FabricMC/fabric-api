package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import net.fabricmc.fabric.impl.entity.event.effect.EffectEventContextImpl;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;

@Mixin(Commands.class)
public final class CommandsMixin {
	private CommandsMixin() {
	}

	@Inject(
			method = "performCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V"
			)
	)
	private void onExecute(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci, @Local ContextChain<CommandSourceStack> contextChain) {
		if (!(contextChain.getTopContext().getNodes().getFirst().getNode() instanceof LiteralCommandNode<CommandSourceStack> commandNode)) {
			return;
		}

		String name = commandNode.getName();
		MobEffectUtil.CURRENT_COMMAND_CONTEXT.set(new EffectEventContextImpl(true, name));
	}

	@Inject(
			method = "performCommand",
			at = @At("RETURN")
	)
	private void afterExecute(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci) {
		MobEffectUtil.CURRENT_COMMAND_CONTEXT.set(EffectEventContextImpl.DEFAULT);
	}
}
