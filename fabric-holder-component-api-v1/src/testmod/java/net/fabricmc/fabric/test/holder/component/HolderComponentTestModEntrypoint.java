package net.fabricmc.fabric.test.holder.component;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class HolderComponentTestModEntrypoint implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			HolderComponentCommand.register(dispatcher, buildContext);
		});
	}
}
