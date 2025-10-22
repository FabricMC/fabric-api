package net.fabricmc.fabric.mixin.inventory;

import net.fabricmc.fabric.api.inventory.InventoryEvents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link ScreenHandler} to inject inventory event handling.
 *
 * <p>Intercepts slot click events to trigger the {@link InventoryEvents#SLOT_CLICK_EVENT} before vanilla processing,
 * allowing modders to modify or cancel slot interactions in any inventory GUI (e.g., chests, furnaces, crafting tables).
 */
@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
	/**
	 * Injects into {@link ScreenHandler#onSlotClick} to fire the {@link InventoryEvents#SLOT_CLICK_EVENT}.
	 *
	 * <p>This injection occurs at the start of the method, before vanilla logic processes the slot click.
	 * If the event returns {@link ActionResult#FAIL}, the click is canceled, and the client state is synchronized
	 * to prevent desyncs. If {@link ActionResult#SUCCESS} is returned, the event is consumed but vanilla processing continues.
	 * {@link ActionResult#PASS} allows other listeners and vanilla logic to proceed.
	 *
	 * @param slotIndex   The raw slot ID from the packet (0–215 for most GUIs, -1 for outside clicks)
	 * @param button      Mouse button (0 = left, 1 = right) + modifiers
	 * @param actionType  The exact action: PICKUP, QUICK_MOVE, SWAP, etc.
	 * @param player      The player who clicked the slot
	 * @param ci          Callback info for canceling the method
	 */
	@Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
	private void onSlotClick(
			int slotIndex,
			int button,
			SlotActionType actionType,
			PlayerEntity player,
			CallbackInfo ci
	) {
		ScreenHandler handler = (ScreenHandler) (Object) this;
		ScreenHandlerAccessorMixin accessor = (ScreenHandlerAccessorMixin) handler;
		Slot slot = slotIndex >= 0 ? handler.getSlot(slotIndex) : null;
		ItemStack cursor = handler.getCursorStack();
		ActionResult result = InventoryEvents.SLOT_CLICK_EVENT.invoker().interact(
				handler, slot, slotIndex, button, actionType, player, cursor
		);

		if (result == ActionResult.FAIL) {
			ci.cancel();
			// Sync client state to prevent desync
			handler.syncState();
		}
	}
}
