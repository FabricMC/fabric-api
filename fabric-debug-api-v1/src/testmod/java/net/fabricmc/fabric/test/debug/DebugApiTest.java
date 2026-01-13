package net.fabricmc.fabric.test.debug;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.decoration.Mannequin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.debug.v1.EntityDebugSubscriptionRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class DebugApiTest implements ModInitializer {
	public static DebugSubscription<SusDebugInfo> SUS_AVATAR;
	public static boolean DEBUG_SUS_AVATAR = true;

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			SUS_AVATAR = Registry.register(
					BuiltInRegistries.DEBUG_SUBSCRIPTION,
					Identifier.fromNamespaceAndPath(
							"fabric-debug-api-v1-testmod",
							"sus_avatar"
					),
					new DebugSubscription<>(SusDebugInfo.STREAM_CODEC)
			);
			EntityDebugSubscriptionRegistry.<SusDebugInfo, Avatar>register(
					SUS_AVATAR,
					entity -> entity instanceof Avatar,
					avatar -> new SusDebugInfo(
							avatar.getPlainTextName(),
							avatar instanceof Mannequin
					),
					DEBUG_SUS_AVATAR
			);
		}
	}
}
