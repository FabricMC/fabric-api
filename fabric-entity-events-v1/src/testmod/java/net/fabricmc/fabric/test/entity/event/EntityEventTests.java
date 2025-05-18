/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.test.entity.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public final class EntityEventTests implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityEventTests.class);
	public static final RegistryKey<Block> TEST_BED_KEY = RegistryKey.of(
			RegistryKeys.BLOCK,
			Identifier.of("fabric-entity-events-v1-testmod", "test_bed")
	);
	public static final Block TEST_BED = new TestBedBlock(AbstractBlock.Settings.create().strength(1, 1).registryKey(TEST_BED_KEY));
	public static final RegistryKey<Item> DIAMOND_ELYTRA_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fabric-entity-events-v1-testmod", "diamond_elytra"));
	public static final Item DIAMOND_ELYTRA = new Item(new Item.Settings().component(DataComponentTypes.GLIDER, Unit.INSTANCE).equippable(EquipmentSlot.CHEST).registryKey(DIAMOND_ELYTRA_KEY));

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, TEST_BED_KEY, TEST_BED);
		Registry.register(Registries.ITEM, TEST_BED_KEY.getValue(), new BlockItem(TEST_BED, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, TEST_BED_KEY.getValue()))));
		Registry.register(Registries.ITEM, DIAMOND_ELYTRA_KEY, DIAMOND_ELYTRA);

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
			LOGGER.info("Entity {} Killed: {}", entity, killed);
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			LOGGER.info("Moved player {}: [{} -> {}]", player, origin.getRegistryKey().getValue(), destination.getRegistryKey().getValue());
		});

		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
			LOGGER.info("Moved entity {} -> {}: [({} -> {}]", originalEntity, newEntity, origin.getRegistryKey().getValue(), destination.getRegistryKey().getValue());
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			LOGGER.info("Copied data for {} from {} to {}", oldPlayer.getGameProfile().getName(), oldPlayer, newPlayer);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			LOGGER.info("Respawned {}, [{}, {}]", oldPlayer.getGameProfile().getName(), oldPlayer.getWorld().getRegistryKey().getValue(), newPlayer.getWorld().getRegistryKey().getValue());
		});

		// No fall damage if holding a feather in the main hand
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (source.getTypeRegistryEntry().matchesKey(DamageTypes.FALL) && entity.getStackInHand(Hand.MAIN_HAND).isOf(Items.FEATHER)) {
				LOGGER.info("Avoided {} of fall damage by holding a feather", amount);
				return false;
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			LOGGER.info("Entity {} received {} damage from {} (initially dealt {}, blocked {})", entity.getName().getString(), damageTaken, source.getName(), baseDamageTaken, blocked);
		});

		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			LOGGER.info("{} is going to die to {} damage from {} damage source", entity.getName().getString(), amount, source.getName());

			if (entity.getStackInHand(Hand.MAIN_HAND).getItem() == Items.CARROT) {
				entity.setHealth(3.0f);
				return false;
			}

			return true;
		});

		// Test that the legacy event still works
		ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) -> {
			if (player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.APPLE) {
				player.setHealth(3.0f);
				return false;
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			LOGGER.info("{} died due to {} damage source", entity.getName().getString(), source.getName());
		});

		ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, keepEquipment) -> {
			LOGGER.info("{} is being converted to {} [{}]", previous.getName().getString(), converted.getName().getString(), keepEquipment);
		});

		EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
			// Can't sleep if holds blue wool
			if (player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BLUE_WOOL)) {
				return PlayerEntity.SleepFailureReason.OTHER_PROBLEM;
			}

			return null;
		});

		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			LOGGER.info("Entity {} sleeping at {}", entity, sleepingPos);
			BlockState bedState = entity.getWorld().getBlockState(sleepingPos);

			if (bedState.isOf(TEST_BED)) {
				boolean shouldBeOccupied = !entity.getStackInHand(Hand.MAIN_HAND).isOf(Items.ORANGE_WOOL);

				if (bedState.get(TestBedBlock.OCCUPIED) != shouldBeOccupied) {
					throw new AssertionError("Test bed should " + (!shouldBeOccupied ? "not " : "") + "be occupied");
				}
			}
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			LOGGER.info("Entity {} woke up at {}", entity, sleepingPos);
		});

		EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
			return state.isOf(TEST_BED) ? ActionResult.SUCCESS : ActionResult.PASS;
		});

		EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
			return entity.getWorld().getBlockState(sleepingPos).isOf(TEST_BED) ? Direction.NORTH : sleepingDirection;
		});

		EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> {
			// Yellow wool allows to sleep during the day
			if (player.getWorld().isDay() && player.getStackInHand(Hand.MAIN_HAND).isOf(Items.YELLOW_WOOL)) {
				return ActionResult.SUCCESS;
			}

			return ActionResult.PASS;
		});

		EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, sleepingPos, vanillaResult) -> {
			// Green wool allows monsters and red wool always "detects" monsters
			ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

			if (stack.isOf(Items.GREEN_WOOL)) {
				return ActionResult.SUCCESS;
			} else if (stack.isOf(Items.RED_WOOL)) {
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});

		EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) -> {
			// Don't set spawn if holding white wool
			return !player.getStackInHand(Hand.MAIN_HAND).isOf(Items.WHITE_WOOL);
		});

		EntitySleepEvents.ALLOW_RESETTING_TIME.register(player -> {
			// Don't allow resetting time if holding black wool
			return !player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BLACK_WOOL);
		});

		EntitySleepEvents.SET_BED_OCCUPATION_STATE.register((entity, sleepingPos, bedState, occupied) -> {
			// Don't set occupied state if holding orange wool
			return entity.getStackInHand(Hand.MAIN_HAND).isOf(Items.ORANGE_WOOL);
		});

		EntitySleepEvents.MODIFY_WAKE_UP_POSITION.register((entity, sleepingPos, bedState, wakeUpPos) -> {
			// If holding cyan wool, wake up 10 blocks above the bed
			if (entity.getStackInHand(Hand.MAIN_HAND).isOf(Items.CYAN_WOOL)) {
				return Vec3d.ofCenter(sleepingPos).add(0, 10, 0);
			}

			return wakeUpPos;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("addsleeptestwools").executes(context -> {
				addSleepWools(context.getSource().getPlayer());
				return 0;
			}));
		});

		// Block elytra flight when holding a torch in the off-hand.
		EntityElytraEvents.ALLOW.register(entity -> {
			return !entity.getOffHandStack().isOf(Items.TORCH);
		});

		ServerPlayerEvents.JOIN.register(player -> {
			assertOnServerThread(player.getServer());
			LOGGER.info("Observed player {} joining the game", player.getGameProfile().getName());
		});

		ServerPlayerEvents.LEAVE.register(player -> {
			assertOnServerThread(player.getServer());
			LOGGER.info("Observed player {} leaving the game", player.getGameProfile().getName());
		});
	}

	private static void addSleepWools(PlayerEntity player) {
		PlayerInventory inventory = player.getInventory();
		inventory.offerOrDrop(createNamedItem(Items.BLUE_WOOL, "Can't start sleeping"));
		inventory.offerOrDrop(createNamedItem(Items.YELLOW_WOOL, "Sleep whenever"));
		inventory.offerOrDrop(createNamedItem(Items.GREEN_WOOL, "Allow nearby monsters"));
		inventory.offerOrDrop(createNamedItem(Items.RED_WOOL, "Detect nearby monsters"));
		inventory.offerOrDrop(createNamedItem(Items.WHITE_WOOL, "Don't set spawn"));
		inventory.offerOrDrop(createNamedItem(Items.BLACK_WOOL, "Don't reset time"));
		inventory.offerOrDrop(createNamedItem(Items.ORANGE_WOOL, "Don't set occupied state"));
		inventory.offerOrDrop(createNamedItem(Items.CYAN_WOOL, "Wake up high above"));
	}

	private static void assertOnServerThread(MinecraftServer server) {
		if (!server.isOnThread()) {
			throw new AssertionError("Expected the game to be on the server thread, but found " + Thread.currentThread());
		}
	}

	private static ItemStack createNamedItem(Item item, String name) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
		return stack;
	}
}
