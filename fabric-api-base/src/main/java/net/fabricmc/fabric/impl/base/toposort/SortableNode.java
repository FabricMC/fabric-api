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

package net.fabricmc.fabric.impl.base.toposort;

import java.util.ArrayList;
import java.util.List;

public abstract class SortableNode<N extends SortableNode<N>> {
	protected final List<N> subsequentNodes = new ArrayList<>();
	protected final List<N> previousNodes = new ArrayList<>();
	boolean visited = false;

	/**
	 * @return Description of this node, used to print the cycle warning.
	 */
	protected abstract String getDescription();

	protected void addSubsequentNode(N node) {
		this.subsequentNodes.add(node);
	}

	protected void addPreviousNode(N node) {
		this.previousNodes.add(node);
	}

	public static <N extends SortableNode<N>> void link(N first, N second) {
		if (first == second) {
			throw new IllegalArgumentException("Cannot link a node to itself!");
		}

		first.addSubsequentNode(second);
		second.addPreviousNode(first);
	}
}
