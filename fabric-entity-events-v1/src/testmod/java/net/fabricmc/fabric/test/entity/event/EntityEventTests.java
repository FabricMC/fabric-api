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

import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
	public static final ResourceKey<Block> TEST_BED_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fabric-entity-events-v1-testmod", "test_bed")
	);
	public static final Block TEST_BED = new TestBedBlock(BlockBehaviour.Properties.of().strength(1, 1).setId(TEST_BED_KEY));
	public static final ResourceKey<Item> DIAMOND_ELYTRA_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-entity-events-v1-testmod", "diamond_elytra"));
	public static final Item DIAMOND_ELYTRA = new Item(new Item.Properties()
												.component(DataComponents.GLIDER, Unit.INSTANCE)
												.component(
														DataComponents.EQUIPPABLE,
														Equippable.builder(EquipmentSlot.CHEST)
																.setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
																.setAsset(EquipmentAssets.ELYTRA)
																.setDamageOnHurt(false)
																.build()
												).setId(DIAMOND_ELYTRA_KEY));

	private static final Player.BedSleepingProblem SLEEP_FAILURE_REASON = new Player.BedSleepingProblem(Component.literal("Cannot sleep while holding blue wool!"));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, TEST_BED_KEY, TEST_BED);
		Registry.register(BuiltInRegistries.ITEM, TEST_BED_KEY.identifier(), new BlockItem(TEST_BED, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, TEST_BED_KEY.identifier()))));
		Registry.register(BuiltInRegistries.ITEM, DIAMOND_ELYTRA_KEY, DIAMOND_ELYTRA);

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed, damageSource) -> {
			LOGGER.info("Entity {} Killed: {}, source {}", entity, killed, damageSource);
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			LOGGER.info("Moved player {}: [{} -> {}]", player, origin.dimension().identifier(), destination.dimension().identifier());
		});

		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
			LOGGER.info("Moved entity {} -> {}: [({} -> {}]", originalEntity, newEntity, origin.dimension().identifier(), destination.dimension().identifier());
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			LOGGER.info("Copied data for {} from {} to {}", oldPlayer.getGameProfile().name(), oldPlayer, newPlayer);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			LOGGER.info("Respawned {}, [{}, {}]", oldPlayer.getGameProfile().name(), oldPlayer.level().dimension().identifier(), newPlayer.level().dimension().identifier());
		});

		// No fall damage if holding a feather in the main hand
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (source.typeHolder().is(DamageTypes.FALL) && entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.FEATHER)) {
				LOGGER.info("Avoided {} of fall damage by holding a feather", amount);
				return false;
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			LOGGER.info("Entity {} received {} damage from {} (initially dealt {}, blocked {})", entity.getName().getString(), damageTaken, source.getMsgId(), baseDamageTaken, blocked);
		});

		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			LOGGER.info("{} is going to die to {} damage from {} damage source", entity.getName().getString(), amount, source.getMsgId());

			if (entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.CARROT) {
				entity.setHealth(3.0f);
				return false;
			}

			return true;
		});

		// Test that the legacy event still works
		ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) -> {
			if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.APPLE) {
				player.setHealth(3.0f);
				return false;
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			LOGGER.info("{} died due to {} damage source", entity.getName().getString(), source.getMsgId());
		});

		ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, keepEquipment) -> {
			LOGGER.info("{} is being converted to {} [{}]", previous.getName().getString(), converted.getName().getString(), keepEquipment);
		});

		EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
			// Can't sleep if holds blue wool
			if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BLUE_WOOL)) {
				return SLEEP_FAILURE_REASON;
			}

			return null;
		});

		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			LOGGER.info("Entity {} sleeping at {}", entity, sleepingPos);
			BlockState bedState = entity.level().getBlockState(sleepingPos);

			if (bedState.is(TEST_BED)) {
				boolean shouldBeOccupied = !entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.ORANGE_WOOL);

				if (bedState.getValue(TestBedBlock.OCCUPIED) != shouldBeOccupied) {
					throw new AssertionError("Test bed should " + (!shouldBeOccupied ? "not " : "") + "be occupied");
				}
			}
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			LOGGER.info("Entity {} woke up at {}", entity, sleepingPos);
		});

		EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
			return state.is(TEST_BED) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		});

		EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
			return entity.level().getBlockState(sleepingPos).is(TEST_BED) ? Direction.NORTH : sleepingDirection;
		});

		EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, sleepingPos, vanillaResult) -> {
			// Green wool allows monsters and red wool always "detects" monsters
			ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

			if (stack.is(Items.GREEN_WOOL)) {
				return InteractionResult.SUCCESS;
			} else if (stack.is(Items.RED_WOOL)) {
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});

		EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) -> {
			// Don't set spawn if holding white wool
			return !player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WHITE_WOOL);
		});

		EntitySleepEvents.ALLOW_RESETTING_TIME.register(player -> {
			// Don't allow resetting time if holding black wool
			return !player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BLACK_WOOL);
		});

		EntitySleepEvents.SET_BED_OCCUPATION_STATE.register((entity, sleepingPos, bedState, occupied) -> {
			// Don't set occupied state if holding orange wool
			return entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.ORANGE_WOOL);
		});

		EntitySleepEvents.MODIFY_WAKE_UP_POSITION.register((entity, sleepingPos, bedState, wakeUpPos) -> {
			// If holding cyan wool, wake up 10 blocks above the bed
			if (entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.CYAN_WOOL)) {
				return Vec3.atCenterOf(sleepingPos).add(0, 10, 0);
			}

			return wakeUpPos;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("addsleeptestwools").executes(context -> {
				addSleepWools(context.getSource().getPlayer());
				return 0;
			}));
		});

		// Block elytra flight when holding a torch in the off-hand.
		EntityElytraEvents.ALLOW.register(entity -> {
			return !entity.getOffhandItem().is(Items.TORCH);
		});

		ServerPlayerEvents.JOIN.register(player -> {
			assertOnServerThread(player.level().getServer());
			LOGGER.info("Observed player {} joining the game", player.getGameProfile().name());
		});

		ServerPlayerEvents.LEAVE.register(player -> {
			assertOnServerThread(player.level().getServer());
			LOGGER.info("Observed player {} leaving the game", player.getGameProfile().name());
		});
	}

	private static void addSleepWools(Player player) {
		Inventory inventory = player.getInventory();
		inventory.placeItemBackInInventory(createNamedItem(Items.BLUE_WOOL, "Can't start sleeping"));
		inventory.placeItemBackInInventory(createNamedItem(Items.YELLOW_WOOL, "Sleep whenever"));
		inventory.placeItemBackInInventory(createNamedItem(Items.GREEN_WOOL, "Allow nearby monsters"));
		inventory.placeItemBackInInventory(createNamedItem(Items.RED_WOOL, "Detect nearby monsters"));
		inventory.placeItemBackInInventory(createNamedItem(Items.WHITE_WOOL, "Don't set spawn"));
		inventory.placeItemBackInInventory(createNamedItem(Items.BLACK_WOOL, "Don't reset time"));
		inventory.placeItemBackInInventory(createNamedItem(Items.ORANGE_WOOL, "Don't set occupied state"));
		inventory.placeItemBackInInventory(createNamedItem(Items.CYAN_WOOL, "Wake up high above"));
	}

	private static void assertOnServerThread(MinecraftServer server) {
		if (!server.isSameThread()) {
			throw new AssertionError("Expected the game to be on the server thread, but found " + Thread.currentThread());
		}
	}

	private static ItemStack createNamedItem(Item item, String name) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
		return stack;
	}
}
