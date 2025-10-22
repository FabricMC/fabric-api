package net.fabricmc.fabric.api.inventory;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.util.ActionResult;

import org.jetbrains.annotations.ApiStatus;

/**
 * Core entry point for inventory-related events in FabricMC.
 *
 * <p>Register listeners like:
 * <pre>{@code
 * InventoryEvents.SLOT_CLICK_EVENT.register((handler, slot, slotId, clickType, actionType, player, cursor) -> {
 *     // Example: Prevent shift-clicking specific items
 *     if (actionType == SlotActionType.QUICK_MOVE && handler.getType() == ScreenHandlerType.CHEST) {
 *         // Add custom logic here
 *         return ActionResult.FAIL; // Cancel the action
 *     }
 *     return ActionResult.PASS;
 * });
 * </pre>
 *
 * <p>Note: When returning {@code ActionResult.FAIL}, the server cancels the action, but client-side state (e.g., cursor or slot contents) may not automatically sync. Modders should manually sync state if needed using {@code ScreenHandler.syncState()}.
 */
@ApiStatus.Experimental
public final class InventoryEvents {
	/**
	 * Fires for slot clicks in ANY inventory GUI.
	 *
	 * <p>This event occurs before vanilla logic executes, allowing modders to intercept or cancel slot interactions.
	 *
	 * <p>Return:
	 * <ul>
	 *     <li>{@code ActionResult.PASS} → Let vanilla and other listeners handle it</li>
	 *     <li>{@code ActionResult.SUCCESS} → Stop propagation (no further listeners), allow vanilla action</li>
	 *     <li>{@code ActionResult.FAIL} → Cancel the action (client sync may be required)</li>
	 * </ul>
	 */
	public static final Event<SlotClickCallback> SLOT_CLICK_EVENT = EventFactory.createArrayBacked(
			SlotClickCallback.class,
			(listeners) -> (handler, slot, slotId, clickType, actionType, player, cursor) -> {
				for (SlotClickCallback listener : listeners) {
					ActionResult result = listener.interact(handler, slot, slotId, clickType, actionType, player, cursor);
					if (result != ActionResult.PASS) {
						return result;
					}
				}
				return ActionResult.PASS;
			}
	);

	private InventoryEvents() {}
}
