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

package net.fabricmc.fabric.test.screen;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import net.fabricmc.fabric.test.screen.chunk.FabriclandChunkGenerator;
import net.fabricmc.fabric.test.screen.chunk.FabriclandChunkGeneratorConfig;

public class FabriclandScreen extends Screen {
	private static final Text TITLE = Text.literal("Fabricland");

	private static final int BUTTON_WIDTH = 150;
	private static final int LARGE_BUTTON_WIDTH = 210;
	private static final int BUTTON_HEIGHT = 20;

	private final CreateWorldScreen parent;
	private final Random random = Random.create();

	private FabriclandChunkGeneratorConfig config;

	public FabriclandScreen(CreateWorldScreen parent, GeneratorOptionsHolder generatorOptionsHolder) {
		super(TITLE);
		this.parent = parent;

		ChunkGenerator chunkGenerator = generatorOptionsHolder.selectedDimensions().getChunkGenerator();
		this.config = FabriclandChunkGeneratorConfig.from(chunkGenerator);
	}

	@Override
	protected void init() {
		int x = (this.width - LARGE_BUTTON_WIDTH) / 2;

		this.addDrawableChild(createChangeBlockButton("outline", FabriclandChunkGeneratorConfig::outline, (config, outline) -> config.withOutline(outline)).dimensions(x, 80, LARGE_BUTTON_WIDTH, BUTTON_HEIGHT).build());
		this.addDrawableChild(createChangeBlockButton("background", FabriclandChunkGeneratorConfig::background, (config, background) -> config.withBackground(background)).dimensions(x, 105, LARGE_BUTTON_WIDTH, BUTTON_HEIGHT).build());

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			this.parent.getWorldCreator().applyModifier((dynamicRegistryManager, dimensionsRegistryHolder) -> {
				Registry<Biome> biomeRegistry = dynamicRegistryManager.getOrThrow(RegistryKeys.BIOME);
				ChunkGenerator chunkGenerator = new FabriclandChunkGenerator(biomeRegistry, config);

				return dimensionsRegistryHolder.with(dynamicRegistryManager, chunkGenerator);
			});

			this.client.setScreen(this.parent);
		}).dimensions((this.width - BUTTON_WIDTH) / 2, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}

	private ButtonWidget.Builder createChangeBlockButton(String suffix, Function<FabriclandChunkGeneratorConfig, BlockState> getter, BiFunction<FabriclandChunkGeneratorConfig, BlockState, FabriclandChunkGeneratorConfig> setter) {
		Text title = getChangeBlockButtonMessage(suffix, getter.apply(this.config));

		return ButtonWidget.builder(title, button -> {
			Registries.BLOCK.getRandom(this.random).ifPresent(entry -> {
				BlockState next = entry.value().getDefaultState();

				this.config = setter.apply(this.config, next);
				button.setMessage(getChangeBlockButtonMessage(suffix, next));
			});
		});
	}

	private static Text getChangeBlockButtonMessage(String suffix, BlockState state) {
		return Text.translatable("generator.fabric-screen-api-v1-testmod.fabricland." + suffix, state.getBlock().getName());
	}
}
