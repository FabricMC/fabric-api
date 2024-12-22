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

package net.fabricmc.fabric.api.client.model.loading.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.GroupableModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;

/**
 * Contains interfaces for the events that can be used to modify models at different points in the loading and baking
 * process.
 *
 * <p>Example use cases:
 * <ul>
 *     <li>Overriding the model for a particular block state - check if the given identifier matches the identifier
 *     for that block state. If so, return your desired model, otherwise return the given model.</li>
 *     <li>Wrapping a model to override certain behaviors - simply return a new model instance and delegate calls
 *     to the original model as needed.</li>
 * </ul>
 *
 * <p>Phases are used to ensure that modifications occur in a reasonable order, e.g. wrapping occurs after overrides,
 * and separate phases are provided for mods that wrap their own models and mods that need to wrap models of other mods
 * or wrap models arbitrarily.
 *
 * <p>These callbacks are invoked for <b>every single model that is loaded or baked</b>, so implementations should be
 * as efficient as possible.
 */
public final class ModelModifier {
	/**
	 * Recommended phase to use when overriding models, e.g. replacing a model with another model.
	 */
	public static final Identifier OVERRIDE_PHASE = Identifier.of("fabric", "override");
	/**
	 * Recommended phase to use for transformations that need to happen before wrapping, but after model overrides.
	 */
	public static final Identifier DEFAULT_PHASE = Event.DEFAULT_PHASE;
	/**
	 * Recommended phase to use when wrapping models.
	 */
	public static final Identifier WRAP_PHASE = Identifier.of("fabric", "wrap");
	/**
	 * Recommended phase to use when wrapping models with transformations that want to happen last,
	 * e.g. for connected textures or other similar visual effects that should be the final processing step.
	 */
	public static final Identifier WRAP_LAST_PHASE = Identifier.of("fabric", "wrap_last");

	@FunctionalInterface
	public interface OnLoad {
		/**
		 * This handler is invoked to allow modification of an unbaked model right after it is first loaded and before
		 * it is cached.
		 *
		 * <p>If the given model is {@code null}, its corresponding identifier was requested during
		 * {@linkplain ResolvableModel#resolve resolution} but the model was not loaded normally; i.e. through a JSON
		 * file, possibly because that file did not exist. If a non-{@code null} model is returned in this case,
		 * resolution will continue without warnings or errors. This callback can return a {@code null} model, which
		 * has the same meaning as described earlier, so it is unlikely that an implementor should need to return
		 * {@code null} unless directly returning the given model.
		 *
		 * <p>For further information, see the docs of {@link ModelLoadingPlugin.Context#modifyModelOnLoad()}.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyModelOnLoad
		 */
		@Nullable
		UnbakedModel modifyModelOnLoad(@Nullable UnbakedModel model, Context context);

		/**
		 * The context for an on load model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model that was loaded.
			 */
			Identifier id();
		}
	}

	@FunctionalInterface
	public interface BeforeBake {
		/**
		 * This handler is invoked to allow modification of the unbaked model instance right before it is baked.
		 *
		 * <p>For further information, see the docs of {@link ModelLoadingPlugin.Context#modifyModelBeforeBake()}.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyModelBeforeBake
		 */
		UnbakedModel modifyModelBeforeBake(UnbakedModel model, Context context);

		/**
		 * The context for a before bake model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model being baked.
			 */
			Identifier id();

			/**
			 * The settings this model is being baked with.
			 */
			ModelBakeSettings settings();

			/**
			 * The baker being used to bake this model. It can be used to {@linkplain Baker#bake bake models} and
			 * {@linkplain Baker#getSpriteGetter get sprites}. Note that baking a model which was not previously
			 * {@linkplain ResolvableModel.Resolver#resolve resolved} will log a warning and return the missing model.
			 */
			Baker baker();
		}
	}

	@FunctionalInterface
	public interface AfterBake {
		/**
		 * This handler is invoked to allow modification of the baked model instance right after it is baked and before
		 * it is cached.
		 *
		 * @param model the current baked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyModelAfterBake
		 */
		BakedModel modifyModelAfterBake(BakedModel model, Context context);

		/**
		 * The context for an after bake model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model being baked.
			 */
			Identifier id();

			/**
			 * The unbaked model that is being baked.
			 */
			UnbakedModel sourceModel();

			/**
			 * The settings this model is being baked with.
			 */
			ModelBakeSettings settings();

			/**
			 * The baker being used to bake this model. It can be used to {@linkplain Baker#bake bake models} and
			 * {@linkplain Baker#getSpriteGetter get sprites}. Note that baking a model which was not previously
			 * {@linkplain ResolvableModel.Resolver#resolve resolved} will log a warning and return the missing model.
			 */
			Baker baker();
		}
	}

	@FunctionalInterface
	public interface OnLoadBlock {
		/**
		 * This handler is invoked to allow modification of an unbaked block model right after it is first loaded.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyBlockModelOnLoad
		 */
		GroupableModel modifyModelOnLoad(GroupableModel model, Context context);

		/**
		 * The context for an on load block model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model that was loaded.
			 */
			ModelIdentifier id();

			/**
			 * The corresponding block state of the model that was loaded.
			 */
			BlockState state();
		}
	}

	@FunctionalInterface
	public interface BeforeBakeBlock {
		/**
		 * This handler is invoked to allow modification of the unbaked block model instance right before it is baked.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyBlockModelBeforeBake
		 */
		GroupableModel modifyModelBeforeBake(GroupableModel model, Context context);

		/**
		 * The context for a before bake block model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model being baked.
			 */
			ModelIdentifier id();

			/**
			 * The baker being used to bake this model. It can be used to {@linkplain Baker#bake bake models} and
			 * {@linkplain Baker#getSpriteGetter get sprites}. Note that baking a model which was not previously
			 * {@linkplain ResolvableModel.Resolver#resolve resolved} will log a warning and return the missing model.
			 */
			Baker baker();
		}
	}

	@FunctionalInterface
	public interface AfterBakeBlock {
		/**
		 * This handler is invoked to allow modification of the baked block model instance right after it is baked.
		 *
		 * @param model the current baked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyBlockModelAfterBake
		 */
		BakedModel modifyModelAfterBake(BakedModel model, Context context);

		/**
		 * The context for an after bake block model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model being baked.
			 */
			ModelIdentifier id();

			/**
			 * The unbaked model that is being baked.
			 */
			GroupableModel sourceModel();

			/**
			 * The baker being used to bake this model. It can be used to {@linkplain Baker#bake bake models} and
			 * {@linkplain Baker#getSpriteGetter get sprites}. Note that baking a model which was not previously
			 * {@linkplain ResolvableModel.Resolver#resolve resolved} will log a warning and return the missing model.
			 */
			Baker baker();
		}
	}

	private ModelModifier() { }
}
