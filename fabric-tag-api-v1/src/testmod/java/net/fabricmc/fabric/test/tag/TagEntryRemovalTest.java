package net.fabricmc.fabric.test.tag;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

public final class TagEntryRemovalTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagEntryRemovalTest.class);

	private final TagKey<Item> TEST_TAG = TagTestUtils.tagKey(RegistryKeys.ITEM, "tag_with_snowballs_but_not_bricks");

	@Override
	public void onInitialize() {
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			if (client) {
				return;
			}

			LOGGER.info("Running tag entry removal tests...");
			TagTestUtils.assertTagContent(LOGGER, "Tag {} / {} Contains expected entries", registries, List.of(TEST_TAG), TagTestUtils::getItemKey, Items.SNOWBALL);
			TagTestUtils.assertThrows(
					() -> TagTestUtils.assertTagContent(LOGGER, "Tag {} Contains expected entries", registries, List.of(TEST_TAG), TagTestUtils::getItemKey, Items.BRICK),
					"Expected %s not to contain bricks".formatted(TEST_TAG)
			);
			LOGGER.info("Tag entry removal tests completed successfully!");
		});
	}
}
