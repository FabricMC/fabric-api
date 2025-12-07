package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.tasks.BuildContexts;

import net.fabricmc.fabric.impl.entity.event.effect.EffectEventContextImpl;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;

@Mixin(CommandQueueEntry.class)
public final class CommandQueueEntryMixin<T extends ExecutionCommandSource<T>, S> {
	@Shadow
	@Final
	private EntryAction<T> action;

	private CommandQueueEntryMixin() {
	}

	@WrapMethod(method = "execute")
	private void onExecute(ExecutionContext<T> executionContext, Operation<Void> original) {
		// if this isn't the case, then this is a function call, not a command call
		if (!(this.action instanceof BuildContexts.TopLevel<T> topLevel)) {
			original.call(executionContext);
			return;
		}

		// if this isn't a LiteralCommandNode, we have bigger problems
		// since this is the first node
		if (!(((ParsedCommandNode<S>) ((BuildContextsAccessor<?>) topLevel).getCommand().getTopContext().getNodes().getFirst()).getNode() instanceof LiteralCommandNode<S> commandNode)) {
			original.call(executionContext);
			return;
		}

		try {
			MobEffectUtil.CURRENT_COMMAND_CONTEXT.get().push(new EffectEventContextImpl(
					true,
					commandNode.getName()
			));

			original.call(executionContext);
		} finally {
			MobEffectUtil.CURRENT_COMMAND_CONTEXT.get().pop();
		}
	}
}
