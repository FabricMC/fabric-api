package net.fabricmc.fabric.mixin.event.interaction.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
	private void wrapSelectedSlot(Inventory instance, int selected, Operation<Void> original) {
		int currentSlot = instance.getSelectedSlot();
		boolean allow = ClientItemScrollEvents.ALLOW.invoker().allowScroll(instance, currentSlot, selected);

		if (allow) {
			ClientItemScrollEvents.BEFORE.invoker().beforeScroll(instance, currentSlot, selected);
			original.call(instance, selected);
			ClientItemScrollEvents.AFTER.invoker().afterScroll(instance, currentSlot, selected);
		}
	}
}
