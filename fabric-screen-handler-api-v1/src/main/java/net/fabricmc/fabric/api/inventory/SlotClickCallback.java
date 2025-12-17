package net.fabricmc.fabric.api.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;

/**
 * Callback interface for handling slot click events in inventory GUIs.
 *
 * <p>Runs BEFORE any slot click is processed to allow interception or cancellation of the action.
 *
 * <p>Use cases:
 * <ul>
 *     <li>Prevent shift-clicking protected items from containers</li>
 *     <li>Implement cooldowns for container access</li>
 *     <li>Perform permission checks for locked containers</li>
 * </ul>
 *
 * <p>Canceling with {@code ActionResult.FAIL} stops the click entirely. Returning {@code ActionResult.SUCCESS} allows the action but consumes the event.
 */
@FunctionalInterface
public interface SlotClickCallback {
	/**
	 * Called when a player clicks a slot in an inventory GUI.
	 *
	 * @param handler    ScreenHandler (e.g., chest, furnace, crafting table)
	 * @param slot       Clicked slot (null if clicked outside inventory)
	 * @param slotId     Raw slot ID from packet (0–215 for most GUIs, -1 for outside clicks)
	 * @param button  Mouse button (0 = left, 1 = right) + modifiers
	 * @param actionType Exact action: PICKUP, QUICK_MOVE, SWAP, etc.
	 * @param player     The player who clicked (useful for permission checks)
	 * @param cursor     Current cursor stack (what the player is holding, never null)
	 * @return           PASS = continue to next listener, SUCCESS/FAIL = stop event chain
	 */
	ActionResult interact(
			ScreenHandler handler,
			Slot slot,
			int slotId,
			int button,
			SlotActionType actionType,
			PlayerEntity player,
			ItemStack cursor
	);
}
