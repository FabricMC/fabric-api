package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class HudStatusBarHeightRegistryImpl implements ClientModInitializer {
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
	static final ToIntFunction<PlayerEntity> ZERO = (PlayerEntity player) -> 0;
	/**
	 * Height provider for the vanilla health bar.
	 *
	 * <p>Mods that would otherwise have a mixin for this are encouraged to instead register a replacement provider via
	 * {@link HudStatusBarHeightRegistry#addLeft(Identifier, ToIntFunction)}.
	 */
	static final ToIntFunction<PlayerEntity> HEALTH_BAR = (PlayerEntity player) -> {
		InGameHud gui = MinecraftClient.getInstance().inGameHud;
		int playerHealth = MathHelper.ceil(player.getHealth());
		int displayHealth = gui.renderHealthValue;
		float maxHealth = Math.max((float) player.getAttributeValue(EntityAttributes.MAX_HEALTH),
				Math.max(displayHealth, playerHealth));
		int absorptionAmount = MathHelper.ceil(player.getAbsorptionAmount());
		int healthRows = MathHelper.ceil((maxHealth + absorptionAmount) / 2.0F / 10.0F);
		int rowShift = Math.max(10 - (healthRows - 2), 3);
		return 10 + (healthRows - 1) * rowShift;
	};
	/**
	 * Height provider for the vanilla armor bar.
	 *
	 * <p>Mods that would otherwise have a mixin for this are encouraged to instead register a replacement provider via
	 * {@link HudStatusBarHeightRegistry#addLeft(Identifier, ToIntFunction)}.
	 */
	static final ToIntFunction<PlayerEntity> ARMOR_BAR = (PlayerEntity player) -> {
		return player.getArmor() > 0 ? 10 : 0;
	};
	/**
	 * Height provider for the vanilla mount health.
	 *
	 * <p>Mods that would otherwise have a mixin for this are encouraged to instead register a replacement provider via
	 * {@link HudStatusBarHeightRegistry#addRight(Identifier, ToIntFunction)}.
	 */
	static final ToIntFunction<PlayerEntity> MOUNT_HEALTH = (PlayerEntity player) -> {
		InGameHud gui = MinecraftClient.getInstance().inGameHud;
		LivingEntity livingEntity = gui.getRiddenEntity();
		int vehicleMaxHearts = gui.getHeartCount(livingEntity);
		return gui.getHeartRows(vehicleMaxHearts) * 10;
	};
	/**
	 * Height provider for the vanilla food bar.
	 *
	 * <p>Mods that would otherwise have a mixin for this are encouraged to instead register a replacement provider via
	 * {@link HudStatusBarHeightRegistry#addRight(Identifier, ToIntFunction)}.
	 */
	static final ToIntFunction<PlayerEntity> FOOD_BAR = (PlayerEntity player) -> {
		InGameHud gui = MinecraftClient.getInstance().inGameHud;
		LivingEntity livingEntity = gui.getRiddenEntity();
		return gui.getHeartCount(livingEntity) == 0 ? 10 : 0;
	};
	/**
	 * Height provider for the vanilla air bar.
	 *
	 * <p>Mods that would otherwise have a mixin for this are encouraged to instead register a replacement provider via
	 * {@link HudStatusBarHeightRegistry#addRight(Identifier, ToIntFunction)}.
	 */
	static final ToIntFunction<PlayerEntity> AIR_BAR = (PlayerEntity player) -> {
		int maxAirSupply = player.getMaxAir();
		int airSupply = Math.clamp(player.getAir(), 0, maxAirSupply);
		boolean isInWater = player.isSubmergedIn(FluidTags.WATER);
		return isInWater || airSupply < maxAirSupply ? 10 : 0;
	};
	/**
	 * This serves two purposes: it provides a fixed order for some vanilla status bars; and it provides reduced vanilla
	 * height providers, to compare with the actual height providers during rendering for potential translations for
	 * vanilla status bars. Translations are achieved via pose stack transformations; alternatively can also be
	 * implemented via mixins.
	 *
	 * <p>Do not use {@link Map}, it does not preserve insertion order.
	 */
	static final Map<Identifier, ToIntFunction<PlayerEntity>> VANILLA_HEIGHT_PROVIDERS = ImmutableMap.of(
			VanillaHudElements.HEALTH_BAR,
			ZERO,
			VanillaHudElements.ARMOR_BAR,
			HEALTH_BAR,
			VanillaHudElements.MOUNT_HEALTH,
			ZERO,
			VanillaHudElements.FOOD_BAR,
			ZERO,
			VanillaHudElements.AIR_BAR,
			reduceToIntFunctions(MOUNT_HEALTH, FOOD_BAR, Integer::sum));
	/**
	 * Height providers registered for the left side above the hotbar.
	 *
	 * <p>Used for checking if any custom height providers have been registered to potentially skip resolving later on.
	 *
	 * <p>Do not use {@link Map}, it does not preserve insertion order.
	 */
	static final Map<Identifier, ToIntFunction<PlayerEntity>> VANILLA_LEFT_HEIGHT_PROVIDERS = ImmutableMap.of(
			VanillaHudElements.HEALTH_BAR,
			HEALTH_BAR,
			VanillaHudElements.ARMOR_BAR,
			ARMOR_BAR);
	/**
	 * Height providers registered for the right side above the hotbar.
	 *
	 * <p>Used for checking if any custom height providers have been registered to potentially skip resolving later on.
	 *
	 * <p>Do not use {@link Map}, it does not preserve insertion order.
	 */
	static final Map<Identifier, ToIntFunction<PlayerEntity>> VANILLA_RIGHT_HEIGHT_PROVIDERS = ImmutableMap.of(
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
	static final Map<Identifier, ToIntFunction<PlayerEntity>> LEFT_HEIGHT_PROVIDERS = new HashMap<>(
			VANILLA_LEFT_HEIGHT_PROVIDERS);
	/**
	 * Height providers registered for the right side above the hotbar, like food and air bubbles.
	 *
	 * <p>The height providers registered here simply return the height of the corresponding status bar.
	 */
	static final Map<Identifier, ToIntFunction<PlayerEntity>> RIGHT_HEIGHT_PROVIDERS = new HashMap<>(
			VANILLA_RIGHT_HEIGHT_PROVIDERS);

	/**
	 * Height providers used during rendering computed from everything that was registered.
	 *
	 * <p>These providers do NOT
	 * return the heights of individual elements; instead they return the height at which an element should render at,
	 * which is computed by summing all the heights from providers considered "below" an element.
	 */
	@Nullable
	static Map<Identifier, ToIntFunction<PlayerEntity>> resolvedHeightProviders;

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register((MinecraftClient minecraft) -> {
			HudStatusBarHeightRegistryImpl.init();
		});
	}

	static void init() {
		// skip resolving if no custom height providers have been registered
		if (VANILLA_LEFT_HEIGHT_PROVIDERS.equals(LEFT_HEIGHT_PROVIDERS)
				&& VANILLA_RIGHT_HEIGHT_PROVIDERS.equals(RIGHT_HEIGHT_PROVIDERS)) {
			resolvedHeightProviders = Map.of();
		} else {
			ImmutableMap.Builder<Identifier, ToIntFunction<PlayerEntity>> builder = ImmutableMap.builder();
			ToIntFunction<PlayerEntity> maxLeftHeightProvider = resolveHeightProviders(LEFT_HEIGHT_PROVIDERS,
					builder::put);
			ToIntFunction<PlayerEntity> maxRightHeightProvider = resolveHeightProviders(RIGHT_HEIGHT_PROVIDERS,
					builder::put);
			resolvedHeightProviders = builder.build();
			applyVanillaHeightProviders(resolvedHeightProviders,
					reduceToIntFunctions(maxLeftHeightProvider, maxRightHeightProvider, Math::max));
		}
	}

	public static void addLeft(Identifier id, ToIntFunction<PlayerEntity> heightProvider) {
		if (resolvedHeightProviders == null) {
			LEFT_HEIGHT_PROVIDERS.put(id, heightProvider);
		} else {
			throw new IllegalStateException("Height provider registry already frozen!");
		}
	}

	public static void addRight(Identifier id, ToIntFunction<PlayerEntity> heightProvider) {
		if (resolvedHeightProviders == null) {
			RIGHT_HEIGHT_PROVIDERS.put(id, heightProvider);
		} else {
			throw new IllegalStateException("Height provider registry already frozen!");
		}
	}

	public static int getHeight(Identifier id) {
		if (resolvedHeightProviders == null) {
			throw new IllegalStateException("Trying to get status bar height for " + id + " too early");
		}

		if (!resolvedHeightProviders.containsKey(id)) {
			throw new IllegalArgumentException("Unknown status bar: " + id);
		}

		PlayerEntity player = MinecraftClient.getInstance().inGameHud.getCameraPlayer();

		if (player == null) {
			throw new IllegalStateException("Trying to get status bar height for " + id + " without a camera player");
		}

		return DEFAULT_HEIGHT + resolvedHeightProviders.get(id).applyAsInt(player);
	}

	private static ToIntFunction<PlayerEntity> resolveHeightProviders(Map<Identifier, ToIntFunction<PlayerEntity>> heightProviderLookup, BiConsumer<Identifier, ToIntFunction<PlayerEntity>> heightProviderConsumer) {
		// called individually for both status bar sides for combining all height providers with the ones below them
		// finally returns a provider for the total height of all providers on this side
		SequencedCollection<Identifier> orderedHeightProviders = getOrderedHeightProviders(heightProviderLookup);

		for (Identifier resourceLocation : heightProviderLookup.keySet()) {
			ToIntFunction<PlayerEntity> heightProvider = resolveHeightProvider(resourceLocation,
					heightProviderLookup,
					orderedHeightProviders);
			heightProviderConsumer.accept(resourceLocation, heightProvider);
		}

		return resolveMaximumHeightProvider(orderedHeightProviders.getLast(),
				heightProviderLookup,
				orderedHeightProviders);
	}

	private static SequencedCollection<Identifier> getOrderedHeightProviders(Map<Identifier, ToIntFunction<PlayerEntity>> heightProviderLookup) {
		// creates an ordered list of all height provider identifiers from the lookup,
		// with a fixed order provided for some vanilla elements and other elements attached to those via the static map;
		// all other elements are simply appended in the order they appear in the hud element registry
		LinkedHashSet<Identifier> orderedHeightProviders = new LinkedHashSet<>();

		for (Identifier resourceLocation : VANILLA_HEIGHT_PROVIDERS.keySet()) {
			for (HudLayer hudLayer : HudElementRegistryImpl.ROOT_ELEMENTS.get(resourceLocation).layers()) {
				if (heightProviderLookup.containsKey(hudLayer.id())) {
					orderedHeightProviders.add(hudLayer.id());
				}
			}
		}

		for (Map.Entry<Identifier, HudElementRegistryImpl.RootLayer> entry : HudElementRegistryImpl.ROOT_ELEMENTS.entrySet()) {
			if (!VANILLA_HEIGHT_PROVIDERS.containsKey(entry.getKey())) {
				for (HudLayer hudLayer : entry.getValue().layers()) {
					if (heightProviderLookup.containsKey(hudLayer.id())) {
						orderedHeightProviders.add(hudLayer.id());
					}
				}
			}
		}

		Set<Identifier> unregisteredHudElements = Sets.difference(heightProviderLookup.keySet(), orderedHeightProviders);

		if (!unregisteredHudElements.isEmpty()) {
			throw new IllegalStateException("Unregistered hud elements: " + unregisteredHudElements);
		}

		return orderedHeightProviders;
	}

	private static ToIntFunction<PlayerEntity> resolveHeightProvider(Identifier resourceLocation, Map<Identifier, ToIntFunction<PlayerEntity>> heightProviderLookup, SequencedCollection<Identifier> orderedHeightProviders) {
		// combines all height providers "below" a hud element for determining the height at which it should render at
		ToIntFunction<PlayerEntity> heightProvider = ZERO;

		for (Identifier heightProviderLocation : orderedHeightProviders) {
			if (heightProviderLocation.equals(resourceLocation)) {
				return heightProvider;
			} else if (heightProviderLookup.containsKey(heightProviderLocation)) {
				heightProvider = reduceToIntFunctions(heightProvider,
						heightProviderLookup.get(heightProviderLocation),
						Integer::sum);
			}
		}

		throw new IllegalStateException();
	}

	private static ToIntFunction<PlayerEntity> resolveMaximumHeightProvider(Identifier resourceLocation, Map<Identifier, ToIntFunction<PlayerEntity>> heightProviderLookup, SequencedCollection<Identifier> orderedHeightProviders) {
		// combines all height providers "below" and including a hud element
		ToIntFunction<PlayerEntity> heightProvider = resolveHeightProvider(resourceLocation,
				heightProviderLookup,
				orderedHeightProviders);
		return reduceToIntFunctions(heightProviderLookup.get(resourceLocation), heightProvider, Integer::sum);
	}

	private static <T> ToIntFunction<T> reduceToIntFunctions(ToIntFunction<T> first, ToIntFunction<T> second, IntBinaryOperator operator) {
		return (T t) -> operator.applyAsInt(first.applyAsInt(t), second.applyAsInt(t));
	}

	private static void applyVanillaHeightProviders(Map<Identifier, ToIntFunction<PlayerEntity>> resolvedHeightProviders, ToIntFunction<PlayerEntity> maxHeightProvider) {
		// wrap vanilla status bars with pose stack transformations to implement potentially altered height values
		for (Map.Entry<Identifier, ToIntFunction<PlayerEntity>> entry : VANILLA_HEIGHT_PROVIDERS.entrySet()) {
			ToIntFunction<PlayerEntity> actualHeightProvider = resolvedHeightProviders.get(entry.getKey());
			ToIntFunction<PlayerEntity> expectedHeightProvider = entry.getValue();
			replaceVanillaElement(entry.getKey(),
					reduceToIntFunctions(expectedHeightProvider, actualHeightProvider, (int i1, int i2) -> i1 - i2));
		}

		// offset text above hotbar depending on height values
		replaceVanillaElement(VanillaHudElements.HELD_ITEM_TOOLTIP,
				(PlayerEntity player) -> HELD_ITEM_TOOLTIP_HEIGHT -
						Math.max(HELD_ITEM_TOOLTIP_HEIGHT, maxHeightProvider.applyAsInt(player)));
		replaceVanillaElement(VanillaHudElements.OVERLAY_MESSAGE,
				(PlayerEntity player) -> OVERLAY_MESSAGE_HEIGHT -
						Math.max(OVERLAY_MESSAGE_HEIGHT, maxHeightProvider.applyAsInt(player) + TEXT_HEIGHT_DELTA));
	}

	private static void replaceVanillaElement(Identifier resourceLocation, ToIntFunction<PlayerEntity> heightProvider) {
		HudElementRegistry.replaceElement(resourceLocation, (HudElement layer) -> {
			return (DrawContext context, RenderTickCounter tickCounter) -> {
				PlayerEntity player = MinecraftClient.getInstance().inGameHud.getCameraPlayer();
				int height = player != null ? heightProvider.applyAsInt(player) : 0;

				if (height != 0) {
					context.getMatrices().pushMatrix();
					context.getMatrices().translate(0.0F, height);
				}
				layer.render(context, tickCounter);

				if (height != 0) {
					context.getMatrices().popMatrix();
				}
			};
		});
	}
}
