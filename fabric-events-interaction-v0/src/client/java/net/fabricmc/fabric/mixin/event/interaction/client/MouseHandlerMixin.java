package net.fabricmc.fabric.mixin.event.interaction.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.event.client.player.ClientItemScrollEvents;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@WrapOperation(
			method = "onScroll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V")
	)
	private void wrapSelectedSlot(
			Inventory instance,
			int selected,
			Operation<Void> original,
			// we must use scaled offsets so that the scroll sensitivity applies
			@Local(name = "scaledXOffset") double scaledXOffset,
			@Local(name = "scaledYOffset") double scaledYOffset
	) {
		int currentSlot = instance.getSelectedSlot();
		boolean allow = ClientItemScrollEvents.ALLOW.invoker().allowScroll(instance, currentSlot, selected, scaledXOffset, scaledYOffset);

		if (allow) {
			ClientItemScrollEvents.BEFORE.invoker().beforeScroll(instance, currentSlot, selected, scaledXOffset, scaledYOffset);
			original.call(instance, selected);
			ClientItemScrollEvents.AFTER.invoker().afterScroll(instance, currentSlot, selected, scaledXOffset, scaledYOffset);
		}
	}
}
