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

/// The Fabric Tag API for working with {@linkplain net.minecraft.tags.TagKey tags}.
/// # Aliasing tags
/// <dfn>Tag alias groups</dfn> are lists of tags that refer to the same set of registry entries.
/// The contained tags will be linked together and get the combined set of entries
/// of all the aliased tags in a group.
///
/// Tag alias groups can be defined in data packs in the `data/<mod namespace>/fabric/tag_alias/<registry>`
/// directory. `<registry>` is the path of the registry's ID, prefixed with `<registry's namespace>/` if it's
/// not {@value net.minecraft.resources.Identifier#DEFAULT_NAMESPACE}. For example, an alias group for block tags would be placed
/// in `data/<mod namespace>/fabric/tag_alias/block/`.
///
/// The JSON format of tag alias groups is an object with a `tags` list. The list contains plain tag IDs with
/// no `#` prefix.
///
/// If multiple tag alias groups include a tag, the groups will be combined and each tag will be an alias
/// for the same contents.
/// ## Tag aliases in the `c` namespace
///
/// For the names of shared `c` tag alias groups, it's important that you use a short and descriptive name.
/// A good way to do this is reusing the name of a contained `c` tag that follows the naming conventions.
/// For example, if the tag alias group contains the tags `c:flowers/tall` and `minecraft:tall_flowers`,
/// the tag alias file should be named `flowers/tall.json`, like the contained `c` tag.
///
/// Tag alias groups in the `c` namespace are primarily intended for merging a `c` tag
/// with an equivalent vanilla tag with no potentially unwanted gameplay behavior. If a vanilla tag affects
/// game mechanics (such as the water tag affecting swimming), don't alias it as a `c` tag.
///
/// If you want to have the contents of a `c` tag in your own tag, prefer including the `c` tag
/// in your tag file directly. That way, data packs can modify your tag separately. Tag aliases make their contained
/// tags almost fully indistinguishable since they get the exact same content, and you have to override the alias group
/// in a higher-priority data pack to unlink them.
@NullMarked
package net.fabricmc.fabric.api.tag.v1;

import org.jspecify.annotations.NullMarked;
