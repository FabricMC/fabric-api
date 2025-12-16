package net.fabricmc.fabric.test.renderer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ItemWithBlock extends BlockItem {
	public static final Map<Identifier, ItemWithBlock> LOOKUP = new HashMap<>();
	private final Item item;

	public ItemWithBlock(Properties properties, Item item, Block block) {
		super(block, properties);
		this.item = item;
		LOOKUP.put(properties.effectiveModel(), this);
	}

	public Item getItem() {
		return item;
	}
}
