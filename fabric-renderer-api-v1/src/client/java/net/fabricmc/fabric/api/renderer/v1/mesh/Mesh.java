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

package net.fabricmc.fabric.api.renderer.v1.mesh;

/**
 * An immutable bundle of {@linkplain QuadView quads} encoded by the renderer, typically via
 * {@link MutableMesh#immutableCopy()}.
 *
 * <p>All declared methods in this interface and inherited methods from {@link MeshView} are thread-safe and may be used
 * concurrently.
 *
 * <p>Only the renderer should implement or extend this interface.
 *
 * @see MeshView
 * @see MutableMesh
 */
public interface Mesh extends MeshView {
}
