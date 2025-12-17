package net.fabricmc.fabric.mixin.inventory;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for private fields in {@link ScreenHandler} used by inventory events.
 *
 * <p>Provides access to the list of all slot instances in the current GUI screen, including player inventory and container-specific slots.
 */
@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessorMixin {
	/**
	 * Gets the list of all slots in this ScreenHandler.
	 *
	 * @return A DefaultedList of Slot objects representing all slots in the GUI
	 */
	@Accessor("slots")
	DefaultedList<Slot> getSlots();
}
