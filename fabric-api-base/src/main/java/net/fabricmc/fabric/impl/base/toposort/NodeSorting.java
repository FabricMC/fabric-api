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
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Contains a topological sort implementation, with tie breaking using a [Comparator].
///
/// The final order is always deterministic (i.e. doesn't change with the order of the input elements or the edges),
/// assuming that they are all different according to the comparator. This also holds in the presence of cycles.
///
/// The steps are as follows:
///
/// 1. Compute node SCCs (Strongly Connected Components, i.e. cycles).
/// 2. Sort nodes within SCCs using the comparator.
/// 3. Sort SCCs with respect to each other by respecting constraints, and using the comparator in case of a tie.
public class NodeSorting {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-base");

	@VisibleForTesting
	public static boolean ENABLE_CYCLE_WARNING = true;

	/// Sort a list of nodes.
	///
	/// @param sortedNodes The list of nodes to sort. Will be modified in-place.
	/// @param elementDescription A description of the elements, used for logging in the presence of cycles.
	/// @param comparator The comparator to break ties and to order elements within a cycle.
	/// @return `true` if all the constraints were satisfied, `false` if there was at least one cycle.
	public static <N extends SortableNode<N>> boolean sort(List<N> sortedNodes, String elementDescription, Comparator<N> comparator) {
		// FIRST KOSARAJU SCC VISIT
		List<N> toposort = new ArrayList<>(sortedNodes.size());

		for (N node : sortedNodes) {
			forwardVisit(node, null, toposort);
		}

		clearStatus(toposort);
		Collections.reverse(toposort);

		// SECOND KOSARAJU SCC VISIT
		Map<N, NodeScc<N>> nodeToScc = new IdentityHashMap<>();

		for (N node : toposort) {
			if (!node.visited) {
				List<N> sccNodes = new ArrayList<>();
				// Collect nodes in SCC.
				backwardVisit(node, sccNodes);
				// Sort nodes by id.
				sccNodes.sort(comparator);
				// Mark nodes as belonging to this SCC.
				NodeScc<N> scc = new NodeScc<>(sccNodes);

				for (N nodeInScc : sccNodes) {
					nodeToScc.put(nodeInScc, scc);
				}
			}
		}

		clearStatus(toposort);

		// Build SCC graph
		for (NodeScc<N> scc : nodeToScc.values()) {
			for (N node : scc.nodes) {
				for (N subsequentNode : node.subsequentNodes) {
					NodeScc<N> subsequentScc = nodeToScc.get(subsequentNode);

					if (subsequentScc != scc) {
						scc.subsequentSccs.add(subsequentScc);
						subsequentScc.inDegree++;
					}
				}
			}
		}

		// Order SCCs according to priorities. When there is a choice, use the SCC with the lowest id.
		// The priority queue contains all SCCs that currently have 0 in-degree.
		PriorityQueue<NodeScc<N>> pq = new PriorityQueue<>(Comparator.comparing(scc -> scc.nodes.get(0), comparator));
		sortedNodes.clear();

		for (NodeScc<N> scc : nodeToScc.values()) {
			if (scc.inDegree == 0) {
				pq.add(scc);
				// Prevent adding the same SCC multiple times, as nodeToScc may contain the same value multiple times.
				scc.inDegree = -1;
			}
		}

		boolean noCycle = true;

		while (!pq.isEmpty()) {
			NodeScc<N> scc = pq.poll();
			sortedNodes.addAll(scc.nodes);

			if (scc.nodes.size() > 1) {
				noCycle = false;

				if (ENABLE_CYCLE_WARNING) {
					// Print cycle warning
					StringBuilder builder = new StringBuilder();
					builder.append("Found cycle while sorting ").append(elementDescription).append(":\n");

					for (N node : scc.nodes) {
						builder.append("\t").append(node.getDescription()).append("\n");
					}

					LOGGER.warn(builder.toString());
				}
			}

			for (NodeScc<N> subsequentScc : scc.subsequentSccs) {
				subsequentScc.inDegree--;

				if (subsequentScc.inDegree == 0) {
					pq.add(subsequentScc);
				}
			}
		}

		return noCycle;
	}

	private static <N extends SortableNode<N>> void forwardVisit(N node, N parent, List<N> toposort) {
		if (!node.visited) {
			// Not yet visited.
			node.visited = true;

			for (N data : node.subsequentNodes) {
				forwardVisit(data, node, toposort);
			}

			toposort.add(node);
		}
	}

	private static <N extends SortableNode<N>> void clearStatus(List<N> nodes) {
		for (N node : nodes) {
			node.visited = false;
		}
	}

	private static <N extends SortableNode<N>> void backwardVisit(N node, List<N> sccNodes) {
		if (!node.visited) {
			node.visited = true;
			sccNodes.add(node);

			for (N data : node.previousNodes) {
				backwardVisit(data, sccNodes);
			}
		}
	}

	private static class NodeScc<N extends SortableNode<N>> {
		final List<N> nodes;
		final List<NodeScc<N>> subsequentSccs = new ArrayList<>();
		int inDegree = 0;

		private NodeScc(List<N> nodes) {
			this.nodes = nodes;
		}
	}
}
