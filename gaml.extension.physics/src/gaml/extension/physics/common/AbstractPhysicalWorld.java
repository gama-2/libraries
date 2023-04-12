/*******************************************************************************************************
 *
 * AbstractPhysicalWorld.java, in simtools.gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.physics.common;

import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import gama.core.metamodel.agent.IAgent;
import gama.core.util.Collector;
import gama.core.util.Collector.AsOrderedSet;
import gaml.core.descriptions.ActionDescription;
import gaml.core.descriptions.ModelDescription;
import gaml.core.statements.Arguments;
import gaml.core.statements.IStatement;
import gaml.extension.physics.gaml.PhysicalSimulationAgent;

/**
 * The Class AbstractPhysicalWorld.
 *
 * @param <WorldType> the generic type
 * @param <ShapeType> the generic type
 * @param <VectorType> the generic type
 */
public abstract class AbstractPhysicalWorld<WorldType, ShapeType, VectorType>
		implements IPhysicalWorld<WorldType, ShapeType, VectorType> {

	/** The simulation. */
	protected final PhysicalSimulationAgent simulation;
	
	/** The world. */
	protected WorldType world;
	
	/** The shape converter. */
	private final IShapeConverter<ShapeType, VectorType> shapeConverter;
	
	/** The contact listener. */
	protected final UniversalContactAddedListener contactListener;
	
	/** The updatable agents. */
	protected final AsOrderedSet<IAgent> updatableAgents = Collector.getOrderedSet();
	
	/** The previous contacts. */
	SetMultimap<IBody, IBody> previousContacts = MultimapBuilder.linkedHashKeys().hashSetValues().build();
	
	/** The new contacts. */
	protected SetMultimap<IBody, IBody> newContacts = MultimapBuilder.linkedHashKeys().hashSetValues().build();

	/** The emit notifications. */
	protected final boolean emitNotifications;

	/**
	 * Instantiates a new abstract physical world.
	 *
	 * @param physicalSimulationAgent the physical simulation agent
	 */
	protected AbstractPhysicalWorld(final PhysicalSimulationAgent physicalSimulationAgent) {
		simulation = physicalSimulationAgent;
		emitNotifications = emitsNotifications(simulation);
		shapeConverter = createShapeConverter();
		contactListener = new UniversalContactAddedListener();
	}

	/**
	 * Creates the world.
	 *
	 * @return the world type
	 */
	protected abstract WorldType createWorld();

	/**
	 * Creates the shape converter.
	 *
	 * @return the i shape converter
	 */
	protected abstract IShapeConverter<ShapeType, VectorType> createShapeConverter();

	@Override
	public void doStep(final Double timeStep, final int maxSubSteps) {
		updateEngine(timeStep, maxSubSteps);
		if (emitNotifications) { updateContacts(); }
		updateAgentsShape();
		updatePositionsAndRotations();
	}

	/**
	 * Update agents shape.
	 */
	protected abstract void updateAgentsShape();

	/**
	 * Update contacts.
	 */
	protected final void updateContacts() {
		// Map<IBody, IBody> newContacts = new HashMap<>();
		collectContacts(newContacts);
		// Check for added contacts... (i.e. not in the previous ones)
		newContacts.forEach((b0, b1) -> {
			if (!previousContacts.containsEntry(b0, b1)) {
				// Tell the listener of a added contact (ContactInfo)
				// System.out.println("Contact envoyé entre " + b0.getAgent() + " et " + b1.getAgent());
				contactUpdate(b0, b1, true);
			} else {
				previousContacts.remove(b0, b1);
			}
		});
		previousContacts.forEach((b0, b1) -> {
			// Tell the listener of a removed contact (ContactInfo)
			// System.out.println("Contact retiré entre " + b0.getAgent() + " et " + b1.getAgent());
			contactUpdate(b0, b1, false);
		});
		previousContacts.clear();
		previousContacts.putAll(newContacts);
		newContacts.clear();

	}

	/**
	 * Needs to be redefined in subclasses if some complementary actions are necessary (like explicitly calling the
	 * collision solver before)
	 *
	 * @param newContacts
	 *            the map where the new contacts should be stored
	 */
	public void collectContacts(final Multimap<IBody, IBody> newContacts) {
		newContacts.putAll(contactListener.getCollectedContacts());
		contactListener.clear();
	}

	/**
	 * Update engine.
	 *
	 * @param timeStep the time step
	 * @param maxSubSteps the max sub steps
	 */
	protected abstract void updateEngine(Double timeStep, int maxSubSteps);

	/**
	 * Emits notifications.
	 *
	 * @param simulation the simulation
	 * @return true, if successful
	 */
	protected boolean emitsNotifications(final IAgent simulation) {
		ModelDescription desc = (ModelDescription) simulation.getSpecies().getDescription();
		return desc.visitMicroSpecies(d -> {
			ActionDescription ad = d.getAction(CONTACT_ADDED);
			boolean a = ad == null || ad.isBuiltIn();
			ad = d.getAction(CONTACT_REMOVED);
			boolean b = ad == null || ad.isBuiltIn();
			return a || b;
		});
	}

	/**
	 * Contact update.
	 *
	 * @param b0 the b 0
	 * @param b1 the b 1
	 * @param added the added
	 */
	protected void contactUpdate(final IBody b0, final IBody b1, final boolean added) {
		String action = added ? CONTACT_ADDED : CONTACT_REMOVED;
		IAgent a0 = b0.getAgent();
		IAgent a1 = b1.getAgent();
		if (a0 == null || a1 == null) return;
		if (!b0.isNoNotification()) {
			IStatement.WithArgs action0 = a0.getSpecies().getAction(action);
			getSimulation().getScope().execute(action0, a0, new Arguments(getSimulation(), Map.of(OTHER, a1)));
		}
		if (!b1.isNoNotification()) {
			IStatement.WithArgs action1 = a1.getSpecies().getAction(action);
			getSimulation().getScope().execute(action1, a1, new Arguments(getSimulation(), Map.of(OTHER, a0)));
		}
	}

	@Override
	public IShapeConverter<ShapeType, VectorType> getShapeConverter() { return shapeConverter; }

	@Override
	public PhysicalSimulationAgent getSimulation() { return simulation; }

	@Override
	public void updateAgentShape(final IAgent agent) {
		updatableAgents.add(agent);
	}

	@Override
	public final WorldType getWorld() {
		if (world == null) { world = createWorld(); }
		return world;
	}

}
