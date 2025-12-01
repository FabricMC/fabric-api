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

/**
 * <h2>The Resource Loader, version 1.</h2>
 *
 * <h3>Quick note about vocabulary in Minecraft:</h3>
 * Resource Pack refers to both client-sided resource pack and data pack
 *
 * <h3>Modded Resource Pack Handling</h3>
 * There are two types of resource packs that mods can provide.
 *
 * <h4>Bundled Resource Pack</h4>
 * Mods can "bundle" resource packs with the mod by putting files inside the {@code assets} or {@code data}
 * sub-directories of the {@code resources} directory. They are always enabled, initially loaded after the vanilla pack,
 * and cannot be disabled. Individual mods' packs are hidden to users in the Resource Pack option screen or the {@code /datapack}
 * command.
 *
 * <h4>Built-in Mod Resource Pack</h4>
 * The Resource Loader adds manually registered mod resource packs. Those resource packs are located inside the
 * {@code resources/resourcepacks/} directory. For example, a built-in data pack with the ID {@code example:test} should be placed
 * under {@code resources/resourcepacks/test/data/}. The packs are then registered with
 * {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#registerBuiltinPack(net.minecraft.resources.Identifier, net.fabricmc.loader.api.ModContainer, net.fabricmc.fabric.api.resource.v1.pack.PackActivationType)}.
 * Users can manually enable or disable the packs, unless it is specified to be always enabled.
 *
 * <h4>Programmer Art and High Contrast Support</h4>
 * Bundled resource packs support Programmer Art and High Contrast vanilla resource packs. Simply place assets
 * under {@code resources/programmer_art/assets/} or {@code resources/high_contrast/assets/}, respectively.
 * Internally, these are treated as a separate internal pack, loaded just after the respective vanilla pack.
 * Toggling the vanilla packs automatically toggles the bundled ones as well; you cannot separately enable or disable them.
 *
 * <h4>Example</h4>
 * Mod A ({@code mod_a} provides a bundled resource pack with both Programmer Art and High Contrast support.
 * Mod B ({@code mod_b}) provides a bundled resource pack and one built-in resource pack, Extra ({@code mod_b:extra}).
 * When neither the Programmer Art nor High Contrast is enabled, the user sees "Vanilla", and "Extra" in the
 * Resource Packs screen. Internally, between Vanilla and Extra packs, two hidden packs exist: {@code mod_a} and {@code mod_b}.
 *
 * <p>Suppose the user then enables both the Programmer Art and High Contrast, and the Resource Packs screen lists
 * "Vanilla", Programmer Art, "Extra", and High Contrast. Internally, there are 4 hidden packs:</p>
 *
 * <ul>
 *     <li>{@code mod_a} and {@code mod_b} between "Vanilla" and Programmer Art.</li>
 *     <li>{@code mod_a_programmer_art}, containing Mod A's Programmer Art assets, just after Programmer Art pack.</li>
 *     <li>{@code mod_a_high_contrast}, containing Mod A's High Contrast assets, just after High Contrast pack.</li>
 * </ul>
 *
 * <p>Note that while the current behavior is to sort bundled resource packs by mod ID in descending order (A to Z), this may change over time.</p>
 *
 * <h3>Resource Reloader</h3>
 * The Resource Loader allows mods to register resource reloaders, also called resource reload listeners through
 * {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#registerReloader(net.minecraft.resources.Identifier, net.minecraft.server.packs.resources.PreparableReloadListener)},
 * which are triggered when resources are reloaded.
 * A resource reloader can be ordered depending on other resource reloaders using {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#addReloaderOrdering(net.minecraft.resources.Identifier, net.minecraft.resources.Identifier)},
 * keys for Vanilla resource reloaders can be found at {@link net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys}.
 */

@NullMarked
package net.fabricmc.fabric.api.resource.v1;

import org.jspecify.annotations.NullMarked;
