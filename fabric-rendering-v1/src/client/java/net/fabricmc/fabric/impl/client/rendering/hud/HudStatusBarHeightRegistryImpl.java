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

package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.StatusBarHeightProvider;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.mixin.client.rendering.HudAccessor;

public final class HudStatusBarHeightRegistryImpl implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabric-rendering-v1");
	/**
	 * The height at which vanilla begins rendering status bars; this is used for health and food / mount health.
	 */
	static final int DEFAULT_HEIGHT = 39;
	/**
	 * The height at which the held item tooltip renders in vanilla; for our purposes we already subtract the default
	 * height.
	 */
	static final int HELD_ITEM_TOOLTIP_HEIGHT = 59 - DEFAULT_HEIGHT;
	/**
	 * The height at which the overlay message (from playing records, or unsuccessfully trying to sleep) renders in
	 * vanilla; for our purposes we already subtract the default height.
	 */
	static final int OVERLAY_MESSAGE_HEIGHT = 68 - DEFAULT_HEIGHT;
	static final int TEXT_HEIGHT_DELTA = OVERLAY_MESSAGE_HEIGHT - HELD_ITEM_TOOLTIP_HEIGHT;
	/**
	 * Height provider for the vanilla health bar.
	 */
	static final StatusBarHeightProvider HEALTH_BAR = player -> {
		Gui hud = Minecraft.getInstance().gui;
		int playerHealth = Mth.ceil(player.getHealth());
		int displayHealth = ((HudAccessor) hud.hud).fabric$getRenderHealthValue();
		float maxHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH),
				Math.max(displayHealth, playerHealth));
		int absorptionAmount = Mth.ceil(player.getAbsorptionAmount());
		int healthRows = Mth.ceil((maxHealth + absorptionAmount) / 2.0F / 10.0F);
		int rowShift = Math.max(10 - (healthRows - 2), 3);
		return 10 + (healthRows - 1) * rowShift;
	};
	/**
	 * Height provider for the vanilla armor bar.
	 */
	static final StatusBarHeightProvider ARMOR_BAR = player -> player.getArmorValue() > 0 ? 10 : 0;
	/**
	 * Height provider for the vanilla mount health.
	 */
	static final StatusBarHeightProvider MOUNT_HEALTH = _ -> {
		Hud hud = Minecraft.getInstance().gui.hud;
		LivingEntity livingEntity = ((HudAccessor) hud).fabric$callGetRiddenEntity();
		int vehicleMaxHearts = ((HudAccessor) hud).fabric$callGetHeartCount(livingEntity);
		return ((HudAccessor) hud).fabric$callGetHeartRows(vehicleMaxHearts) * 10;
	};
	/**
	 * Height provider for the vanilla food bar.
	 */
	static final StatusBarHeightProvider FOOD_BAR = _ -> {
		Hud hud = Minecraft.getInstance().gui.hud;
		LivingEntity livingEntity = ((HudAccessor) hud).fabric$callGetRiddenEntity();
		return ((HudAccessor) hud).fabric$callGetHeartCount(livingEntity) == 0 ? 10 : 0;
	};
	/**
	 * Height provider for the vanilla air bar.
	 */
	static final StatusBarHeightProvider AIR_BAR = player -> {
		int maxAirSupply = player.getMaxAirSupply();
		int airSupply = Math.clamp(player.getAirSupply(), 0, maxAirSupply);
		boolean isInWater = player.isEyeInFluid(FluidTags.WATER);
		return isInWater || airSupply < maxAirSupply ? 10 : 0;
	};
	/**
	 * This serves two purposes: it provides a fixed order for some vanilla status bars; and it provides resolved
	 * vanilla height providers, to compare with the actual height providers during rendering for potential translations
	 * for vanilla status bars. Translations are achieved via pose stack transformations.
	 *
	 * <p>Do not use {@link Map#of()}; it does not preserve insertion order.
	 */
	static final Map<Identifier, YPosProvider> VANILLA_Y_POS_PROVIDERS = ImmutableMap.of(
			VanillaHudElements.HEALTH_BAR,
			YPosProvider.ZERO,
			VanillaHudElements.ARMOR_BAR,
			HEALTH_BAR::getStatusBarHeight,
			VanillaHudElements.MOUNT_HEALTH,
			YPosProvider.ZERO,
			VanillaHudElements.FOOD_BAR,
			YPosProvider.ZERO,
			VanillaHudElements.AIR_BAR,
			reduceToIntFunctions(MOUNT_HEALTH, FOOD_BAR, Integer::sum));
	/**
	 * Height providers registered for the left side above the hotbar.
	 *
	 * <p>Used for checking if any custom height providers have been registered to potentially skip resolving later on.
	 */
	static final Map<Identifier, StatusBarHeightProvider> LEFT_VANILLA_HEIGHT_PROVIDERS = ImmutableMap.of(
			VanillaHudElements.HEALTH_BAR,
			HEALTH_BAR,
			VanillaHudElements.ARMOR_BAR,
			ARMOR_BAR);
	/**
	 * Height providers registered for the right side above the hotbar.
	 *
	 * <p>Used for checking if any custom height providers have been registered to potentially skip resolving later on.
	 */
	static final Map<Identifier, StatusBarHeightProvider> RIGHT_VANILLA_HEIGHT_PROVIDERS = ImmutableMap.of(
			VanillaHudElements.MOUNT_HEALTH,
			MOUNT_HEALTH,
			VanillaHudElements.FOOD_BAR,
			FOOD_BAR,
			VanillaHudElements.AIR_BAR,
			AIR_BAR);
	/**
	 * Height providers registered for the left side above the hotbar, like health and armor.
	 *
	 * <p>The height providers registered here simply return the height of the corresponding status bar.
	 */
	static final Map<Identifier, StatusBarHeightProvider> LEFT_HEIGHT_PROVIDERS = new HashMap<>(
			LEFT_VANILLA_HEIGHT_PROVIDERS);
	/**
	 * Height providers registered for the right side above the hotbar, like food and air bubbles.
	 *
	 * <p>The height providers registered here simply return the height of the corresponding status bar.
	 */
	static final Map<Identifier, StatusBarHeightProvider> RIGHT_HEIGHT_PROVIDERS = new HashMap<>(
			RIGHT_VANILLA_HEIGHT_PROVIDERS);

	/**
	 * Height providers used during rendering computed from everything that was registered.
	 *
	 * <p>These providers do NOT
	 * return the heights of individual elements; instead they return the height at which an element should render at,
	 * which is computed by summing all the heights from providers considered "below" an element.
	 */
	@Nullable
	static Map<Identifier, YPosProvider> yPosProviders;

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_ -> HudStatusBarHeightRegistryImpl.init());
	}

	public static void addLeft(Identifier id, StatusBarHeightProvider heightProvider) {
		if (yPosProviders == null) {
			LEFT_HEIGHT_PROVIDERS.put(id, heightProvider);
		} else {
			throw new IllegalStateException("Height provider registry already frozen!");
		}
	}

	public static void addRight(Identifier id, StatusBarHeightProvider heightProvider) {
		if (yPosProviders == null) {
			RIGHT_HEIGHT_PROVIDERS.put(id, heightProvider);
		} else {
			throw new IllegalStateException("Height provider registry already frozen!");
		}
	}

	public static int getHeight(Identifier id) {
		if (yPosProviders == null) {
			throw new IllegalStateException("Trying to get status bar height for " + id + " too early");
		}

		if (!yPosProviders.containsKey(id)) {
			throw new IllegalArgumentException("Unknown status bar: " + id);
		}

		Player player = ((HudAccessor) Minecraft.getInstance().gui.hud).fabric$callGetCameraPlayer();

		if (player == null) {
			throw new IllegalStateException("Trying to get status bar height for " + id + " without a camera player");
		}

		return DEFAULT_HEIGHT + yPosProviders.get(id).getYPos(player);
	}

	static void init() {
		// skip resolving if no custom height providers have been registered
		if (LEFT_VANILLA_HEIGHT_PROVIDERS.equals(LEFT_HEIGHT_PROVIDERS) && RIGHT_VANILLA_HEIGHT_PROVIDERS.equals(
				RIGHT_HEIGHT_PROVIDERS)) {
			HudStatusBarHeightRegistryImpl.yPosProviders = VANILLA_Y_POS_PROVIDERS;
		} else {
			Map<Identifier, YPosProvider> yPosProviders = new LinkedHashMap<>();
			YPosProvider maxLeftYPosProvider = getYPosProviders(LEFT_HEIGHT_PROVIDERS,
					yPosProviders::put);
			YPosProvider maxRightYPosProvider = getYPosProviders(RIGHT_HEIGHT_PROVIDERS,
					yPosProviders::put);
			applyVanillaYPosProviders(yPosProviders,
					reduceToIntFunctions(maxLeftYPosProvider, maxRightYPosProvider, Math::max));
			HudStatusBarHeightRegistryImpl.yPosProviders = ImmutableMap.copyOf(yPosProviders);
		}
	}

	private static YPosProvider getYPosProviders(Map<Identifier, StatusBarHeightProvider> heightProviderLookup, BiConsumer<Identifier, YPosProvider> yPosProviderConsumer) {
		// called individually for both status bar sides for combining all height providers with the ones below them
		// finally returns a provider for the total height of all providers on this side
		SequencedSet<Identifier> orderedHeightProviders = getOrderedHeightProviders(heightProviderLookup);
		Set<Identifier> unregisteredHudElements = Sets.difference(heightProviderLookup.keySet(),
				orderedHeightProviders);

		if (!unregisteredHudElements.isEmpty()) {
			throw new IllegalStateException("Unregistered hud elements: " + unregisteredHudElements);
		}

		for (Identifier id : heightProviderLookup.keySet()) {
			YPosProvider yPosProvider = resolveYPosProvider(id,
					heightProviderLookup,
					orderedHeightProviders);
			yPosProviderConsumer.accept(id, yPosProvider);
		}

		return resolveMaximumYPosProvider(orderedHeightProviders.getLast(),
				heightProviderLookup,
				orderedHeightProviders);
	}

	private static SequencedSet<Identifier> getOrderedHeightProviders(Map<Identifier, StatusBarHeightProvider> heightProviderLookup) {
		// creates an ordered list of all height provider identifiers from the lookup,
		// with a fixed order provided for some vanilla elements and other elements attached to those via the static map;
		// all other elements are simply appended in the order they appear in the hud element registry
		SequencedSet<Identifier> orderedHeightProviders = new LinkedHashSet<>();

		for (Identifier id : VANILLA_Y_POS_PROVIDERS.keySet()) {
			for (HudLayer hudLayer : HudElementRegistryImpl.ROOT_ELEMENTS.get(id).layers()) {
				addOrderedHeightProvider(hudLayer, heightProviderLookup, orderedHeightProviders::add);
			}
		}

		for (Map.Entry<Identifier, HudElementRegistryImpl.RootLayer> entry : HudElementRegistryImpl.ROOT_ELEMENTS.entrySet()) {
			if (!VANILLA_Y_POS_PROVIDERS.containsKey(entry.getKey())) {
				for (HudLayer hudLayer : entry.getValue().layers()) {
					addOrderedHeightProvider(hudLayer, heightProviderLookup, orderedHeightProviders::add);
				}
			}
		}

		return orderedHeightProviders;
	}

	private static void addOrderedHeightProvider(HudLayer hudLayer, Map<Identifier, StatusBarHeightProvider> heightProviderLookup, Consumer<Identifier> heightProviderConsumer) {
		// height providers for removed layers are skipped, as there is no way to remove them manually
		if (!hudLayer.isRemoved() && heightProviderLookup.containsKey(hudLayer.id())) {
			heightProviderConsumer.accept(hudLayer.id());
		}
	}

	private static YPosProvider resolveYPosProvider(Identifier id, Map<Identifier, StatusBarHeightProvider> heightProviderLookup, SequencedCollection<Identifier> orderedHeightProviders) {
		// combines all height providers "below" a hud element for determining the height at which it should render at
		YPosProvider yPosProvider = YPosProvider.ZERO;

		for (Identifier heightProviderLocation : orderedHeightProviders) {
			if (heightProviderLocation.equals(id)) {
				return yPosProvider;
			} else if (heightProviderLookup.containsKey(heightProviderLocation)) {
				yPosProvider = reduceToIntFunctions(yPosProvider,
						heightProviderLookup.get(heightProviderLocation),
						Integer::sum);
			}
		}

		throw new IllegalStateException("Unknown height provider: " + id);
	}

	private static YPosProvider resolveMaximumYPosProvider(Identifier id, Map<Identifier, StatusBarHeightProvider> heightProviderLookup, SequencedCollection<Identifier> orderedHeightProviders) {
		// combines all height providers "below" and including a hud element
		YPosProvider yPosProvider = resolveYPosProvider(id, heightProviderLookup, orderedHeightProviders);
		return reduceToIntFunctions(heightProviderLookup.get(id), yPosProvider, Integer::sum);
	}

	private static YPosProvider reduceToIntFunctions(ToIntFunction<Player> first, ToIntFunction<Player> second, IntBinaryOperator operator) {
		return (Player player) -> operator.applyAsInt(first.applyAsInt(player), second.applyAsInt(player));
	}

	private static void applyVanillaYPosProviders(Map<Identifier, YPosProvider> yPosProviders, YPosProvider maxYPosProvider) {
		// wrap vanilla status bars with pose stack transformations to implement potentially altered height values
		for (Map.Entry<Identifier, YPosProvider> entry : VANILLA_Y_POS_PROVIDERS.entrySet()) {
			if (isVanillaHeightProvider(entry.getKey())) {
				YPosProvider vanillaYPosProvider = entry.getValue();
				// Replace the actual height provider with the vanilla height provider so that
				// getHeight(Identifier) for a vanilla element returns the vanilla values, instead of the actual values.
				// We return the vanilla values because the pose stack transformations below already shift the position.
				// If we return the actual values, and a user tries to render said vanilla element using getHeight(Identifier),
				// it would get shifted again by the pose stack transformations, resulting in an incorrect position.
				YPosProvider actualYPosProvider = yPosProviders.put(entry.getKey(),
						vanillaYPosProvider);
				Objects.requireNonNull(actualYPosProvider,
						() -> "resolved height provider " + entry.getKey() + " is null");
				replaceVanillaElement(entry.getKey(),
						reduceToIntFunctions(vanillaYPosProvider,
								actualYPosProvider,
								(int i1, int i2) -> i1 - i2));
			} else {
				LOGGER.debug("Skipped wrapping hud element {} for applying height provider offsets", entry.getKey());
			}
		}

		// offset text above hotbar depending on height values
		replaceVanillaElement(VanillaHudElements.HELD_ITEM_TOOLTIP,
				(Player player) -> HELD_ITEM_TOOLTIP_HEIGHT - Math.max(HELD_ITEM_TOOLTIP_HEIGHT,
						maxYPosProvider.getYPos(player)));
		replaceVanillaElement(VanillaHudElements.OVERLAY_MESSAGE,
				(Player player) -> OVERLAY_MESSAGE_HEIGHT - Math.max(OVERLAY_MESSAGE_HEIGHT,
						maxYPosProvider.getYPos(player) + TEXT_HEIGHT_DELTA));
	}

	private static boolean isVanillaHeightProvider(Identifier id) {
		if (LEFT_HEIGHT_PROVIDERS.containsKey(id) && LEFT_HEIGHT_PROVIDERS.get(id) == LEFT_VANILLA_HEIGHT_PROVIDERS.get(
				id)) {
			return true;
		}

		if (RIGHT_HEIGHT_PROVIDERS.containsKey(id)
				&& RIGHT_HEIGHT_PROVIDERS.get(id) == RIGHT_VANILLA_HEIGHT_PROVIDERS.get(id)) {
			return true;
		}

		return false;
	}

	private static void replaceVanillaElement(Identifier id, YPosProvider yPosProvider) {
		HudElementRegistry.replaceElement(id, (HudElement layer) -> (GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) -> {
			Player player = ((HudAccessor) Minecraft.getInstance().gui.hud).fabric$callGetCameraPlayer();
			int height = player != null ? yPosProvider.getYPos(player) : 0;

			if (height != 0) {
				graphics.pose().pushMatrix();
				graphics.pose().translate(0.0F, height);
			}

			layer.extractRenderState(graphics, deltaTracker);

			if (height != 0) {
				graphics.pose().popMatrix();
			}
		});
	}

	/**
	 * Returns the sum of all registered provider heights that are considered "below" the position of the element
	 * associated with the given {@link HudElement}.
	 *
	 * <p>Exists in addition to {@link StatusBarHeightProvider} to help distinguish both functionalities in the
	 * implementation.
	 */
	@FunctionalInterface
	public interface YPosProvider extends ToIntFunction<Player> {
		YPosProvider ZERO = _ -> 0;

		/**
		 * @param player the {@link Player} from {@link Gui#getCameraPlayer()}
		 * @return the vertical space occupied by all status bars "below" this one
		 */
		int getYPos(Player player);

		@ApiStatus.NonExtendable
		@Override
		default int applyAsInt(Player player) {
			return this.getYPos(player);
		}
	}
}
