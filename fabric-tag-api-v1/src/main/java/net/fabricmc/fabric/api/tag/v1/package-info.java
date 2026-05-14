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
 * <h2>Removing entries from tags</h2>
 * <dfn>Tag entry removals</dfn> may be used to remove entries from a tag.
 *
 * <p>These may be used to remove values from gameplay facing tags, to exclude specific entries from
 * referenced tags from being applied via a tag's {@linkplain net.minecraft.registry.tag.TagFile#entries() values}
 * field, or to just remove unwanted values.
 *
 * <p>All tag files contain an additional field: {@code fabric:remove} which is an array of entries
 * you wish to remove, following the same syntax as the {@code values} field.
 *
 * <p>Entries within the {@code fabric:remove} field are handled after all of the current file's values are added to the tag.
 * These entries should never be required, meaning they will never throw exceptions if not present in the associated registry.
 *
 * <p>Tag entries may always be added back by data packs that load after the pack that removes the respective value(s).
 */
package net.fabricmc.fabric.api.tag.v1;
