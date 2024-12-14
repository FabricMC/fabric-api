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
 * The Fabric Tag API for working with {@linkplain net.minecraft.registry.tag.TagKey tags}.
 *
 * <h1>Aliasing tags</h1>
 * <dfn>Tag alias groups</dfn> are lists of tags that refer to the same set of registry entries.
 * The contained tags will be linked together and get the combined set of entries
 * of all the aliased tags in a group.
 *
 * <p>Tag alias groups can be defined in data packs in the {@code data/<mod namespace>/fabric/tag_alias/<registry>}
 * directory. {@code <registry>} is the path of the registry's ID, prefixed with {@code <registry's namespace>/} if it's
 * not {@value net.minecraft.util.Identifier#DEFAULT_NAMESPACE}. For example, an alias group for block tags would be placed
 * in {@code data/<mod namespace>/fabric/tag_alias/block/}.
 *
 * <p>The JSON format of tag alias groups is an object with a {@code tags} list. The list contains plain tag IDs with
 * no {@code #} prefix.
 *
 * <p>If multiple tag alias groups include a tag, the groups will be combined and each tag will be an alias
 * for the same contents.
 *
 * <h2>Tag aliases in the {@code c} namespace</h2>
 *
 * <p>For the names of shared {@code c} tag alias groups, it's important that you use a short and descriptive name.
 * A good way to do this is reusing the name of a contained {@code c} tag that follows the naming conventions.
 * For example, if the tag alias group contains the tags {@code c:flowers/tall} and {@code minecraft:tall_flowers},
 * the tag alias file should be named {@code flowers/tall.json}, like the contained {@code c} tag.
 *
 * <p>Tag alias groups in the {@code c} namespace are primarily intended for merging a {@code c} tag
 * with an equivalent vanilla tag with no potentially unwanted gameplay behavior. If a vanilla tag affects
 * game mechanics (such as the water tag affecting swimming), don't alias it as a {@code c} tag.
 *
 * <p>If you want to have the contents of a {@code c} tag in your own tag, prefer including the {@code c} tag
 * in your tag file directly. That way, data packs can modify your tag separately. Tag aliases make their contained
 * tags almost fully indistinguishable since they get the exact same content, and you have to override the alias group
 * in a higher-priority data pack to unlink them.
 */
package net.fabricmc.fabric.api.tag.v1;
