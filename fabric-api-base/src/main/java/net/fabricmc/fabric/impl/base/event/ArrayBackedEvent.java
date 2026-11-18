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

package net.fabricmc.fabric.impl.base.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;

class ArrayBackedEvent<T> extends Event<T> {
	/**
	 * Single background thread that swaps in generated invokers, see {@link CombinedInvokerFactory}.
	 */
	static final ExecutorService UPGRADES = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r, "Fabric event invoker generator");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		return thread;
	});

	private final Function<T[], T> invokerFactory;
	private final Object lock = new Object();
	private final Class<?> componentType;
	/**
	 * All listeners, grouped by phase in {@link #sortedPhases} order.
	 * This array is never mutated after being published, as invoker factories may keep a reference to it.
	 */
	private T[] handlers;
	/**
	 * Registered event phases.
	 */
	private final Map<Identifier, EventPhaseData<T>> phases = new LinkedHashMap<>();
	/**
	 * Phases sorted in the correct dependency order.
	 */
	private final List<EventPhaseData<T>> sortedPhases = new ArrayList<>();

	/**
	 * The invoker that never changes, if the event has one. It forwards to {@link #invoker}.
	 */
	private final @Nullable StableInvoker<T> stable;

	ArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory) {
		this(type, invokerFactory, null);
	}

	@SuppressWarnings("unchecked")
	ArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory, @Nullable StableInvoker<T> stable) {
		this.invokerFactory = invokerFactory;
		this.componentType = type;
		this.stable = stable;
		this.handlers = (T[]) Array.newInstance(type, 0);
		update();
	}

	/**
	 * The current invoker, even if {@link #invoker()} returns a stable one that forwards to it.
	 */
	T currentInvoker() {
		return invoker;
	}

	private void setInvoker(T newInvoker) {
		this.invoker = newInvoker;

		if (stable != null) {
			stable.retarget(newInvoker);
		}
	}

	void update() {
		T[] current = handlers;
		setInvoker(invokerFactory.apply(current));

		if (invokerFactory instanceof CombinedInvokerFactory<T> unrolled && current.length > 1) {
			scheduleUpgrade(unrolled, current);
		}
	}

	private void scheduleUpgrade(CombinedInvokerFactory<T> unrolled, T[] snapshot) {
		UPGRADES.execute(() -> {
			synchronized (lock) {
				if (handlers != snapshot) return; // stale, a newer update has been scheduled
			}

			T generated = unrolled.generateUnrolled(snapshot);
			if (generated == null) return;

			synchronized (lock) {
				if (handlers == snapshot) {
					setInvoker(generated);
				}
			}
		});
	}

	@Override
	public void register(T listener) {
		register(DEFAULT_PHASE, listener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void register(Identifier phaseIdentifier, T listener) {
		Objects.requireNonNull(phaseIdentifier, "Tried to register a listener for a null phase!");
		Objects.requireNonNull(listener, "Tried to register a null listener!");

		synchronized (lock) {
			EventPhaseData<T> phase = phases.get(phaseIdentifier);

			if (phase == null) {
				List<EventPhaseData<T>> before = new ArrayList<>(sortedPhases);
				phase = createPhase(phaseIdentifier);
				sortPhases(before);
			}

			// Insert at the end of the phase, with a single allocation.
			int offset = 0;

			for (EventPhaseData<T> p : sortedPhases) {
				offset += p.size;
				if (p == phase) break;
			}

			int oldLength = handlers.length;
			T[] newHandlers = (T[]) Array.newInstance(componentType, oldLength + 1);
			System.arraycopy(handlers, 0, newHandlers, 0, offset);
			newHandlers[offset] = listener;
			System.arraycopy(handlers, offset, newHandlers, offset + 1, oldLength - offset);

			phase.size++;
			handlers = newHandlers;
			update();
		}
	}

	private EventPhaseData<T> createPhase(Identifier id) {
		EventPhaseData<T> phase = new EventPhaseData<>(id);
		phases.put(id, phase);
		sortedPhases.add(phase);
		return phase;
	}

	private EventPhaseData<T> getOrCreatePhase(Identifier id) {
		EventPhaseData<T> phase = phases.get(id);
		return phase != null ? phase : createPhase(id);
	}

	/**
	 * Sorts the phases, and rebuilds the invoker only if the relative order of phases that contain listeners changed.
	 *
	 * @param before The phase order before the modification that requires sorting. Must not contain phases created since.
	 */
	@SuppressWarnings("unchecked")
	private void sortPhases(List<EventPhaseData<T>> before) {
		if (sortedPhases.size() < 2) return;

		NodeSorting.sort(sortedPhases, "event phases", Comparator.comparing(data -> data.id));

		// Compare the order of non-empty phases.
		int i = 0;
		int j = 0;
		boolean changed = false;
		int nb = before.size();
		int na = sortedPhases.size();

		while (true) {
			while (i < nb && before.get(i).size == 0) i++;
			while (j < na && sortedPhases.get(j).size == 0) j++;

			if (i >= nb || j >= na) {
				changed = (i < nb) != (j < na);
				break;
			}

			if (before.get(i) != sortedPhases.get(j)) {
				changed = true;
				break;
			}

			i++;
			j++;
		}

		if (!changed) return;

		// Compute the old start of each phase, then copy the slices in the new order.
		int pos = 0;

		for (EventPhaseData<T> p : before) {
			p.start = pos;
			pos += p.size;
		}

		T[] newHandlers = (T[]) Array.newInstance(componentType, handlers.length);
		pos = 0;

		for (EventPhaseData<T> p : sortedPhases) {
			System.arraycopy(handlers, p.start, newHandlers, pos, p.size);
			pos += p.size;
		}

		handlers = newHandlers;
		update();
	}

	@Override
	public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
		Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
		Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
		if (firstPhase.equals(secondPhase)) throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

		synchronized (lock) {
			addOrdering(firstPhase, secondPhase);
		}
	}

	/**
	 * Adds orderings {@code phases[0] -> phases[1] -> ...} with a single sort.
	 */
	void addPhaseOrderings(Identifier[] chain) {
		synchronized (lock) {
			List<EventPhaseData<T>> before = new ArrayList<>(sortedPhases);
			boolean modified = false;

			for (int i = 1; i < chain.length; i++) {
				modified |= link(chain[i - 1], chain[i]);
			}

			if (modified) sortPhases(before);
		}
	}

	private void addOrdering(Identifier firstPhase, Identifier secondPhase) {
		List<EventPhaseData<T>> before = new ArrayList<>(sortedPhases);

		if (link(firstPhase, secondPhase)) {
			sortPhases(before);
		}
	}

	/**
	 * @return {@code true} if a phase or an ordering was added, i.e. if sorting is required.
	 */
	private boolean link(Identifier firstPhase, Identifier secondPhase) {
		int phaseCount = phases.size();
		EventPhaseData<T> first = getOrCreatePhase(firstPhase);
		EventPhaseData<T> second = getOrCreatePhase(secondPhase);
		boolean linked = first.linkTo(second);
		return linked || phases.size() != phaseCount;
	}
}
