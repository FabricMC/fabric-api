//package net.fabricmc.fabric.test.inventory;
//
//import net.fabricmc.api.ModInitializer;
//import net.fabricmc.fabric.api.inventory.InventoryEvents;
//
//import net.minecraft.item.Items;
//import net.minecraft.screen.GenericContainerScreenHandler;
//import net.minecraft.text.Text;
//import net.minecraft.util.ActionResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class InventoryEventsTest implements ModInitializer {
//	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-screen-handler-api-v1-testmod");
//
//	@Override
//	public void onInitialize() {
//		LOGGER.info("Initializing Fabric Inventory Events Test Mod...");
//
//		// Prevent diamonds from being moved
//		InventoryEvents.SLOT_CLICK_EVENT.register((handler, slot, slotId, button, actionType, player, cursor) -> {
//			if (!(handler instanceof GenericContainerScreenHandler) || slot == null) {
//				return ActionResult.PASS;
//			}
//
//			if (slot.getStack().isOf(Items.DIAMOND)) {
//				player.sendMessage(Text.literal("Diamonds are protected and cannot be moved."), false);
//				LOGGER.info("Player {} tried to move diamonds.", player.getName().getString());
//				return ActionResult.FAIL;
//			}
//
//			return ActionResult.PASS;
//		});
//	}
//}
//
