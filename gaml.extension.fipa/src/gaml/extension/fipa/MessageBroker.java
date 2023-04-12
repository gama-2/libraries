/*******************************************************************************************************
 *
 * MessageBroker.java, in msi.gaml.extensions.fipa, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.fipa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gaml.core.types.IType;
import gaml.core.types.Types;

/**
 * The Class MessageBroker.
 *
 * TODO Provide this class with a copy of the scope with which it is created to simplify the API by removing the scope
 * in the parameters
 *
 * @author drogoul
 */
public class MessageBroker {

	/** The messages to deliver. */
	private final Map<IAgent, List<FIPAMessage>> messagesToDeliver = new HashMap<>();

	/**
	 * Centralized storage of Conversations and Messages to facilitate Garbage Collection
	 */
	private final Map<IAgent, ConversationsMessages> conversationsMessages = new HashMap<>();

	/** The instance. */
	private static Map<SimulationAgent, MessageBroker> instances = new HashMap<>();

	/**
	 * @throws GamaRuntimeException
	 *             Deliver message.
	 *
	 * @param m
	 *            the m
	 *
	 * @throws GamlException
	 *             the gaml exception
	 */
	public IList<FIPAMessage> deliverMessagesFor(final IScope scope, final IAgent a) throws GamaRuntimeException {
		final List<FIPAMessage> messagesForA = messagesToDeliver.get(a);
		if (messagesForA == null) { return GamaListFactory.EMPTY_LIST; }

		final IList<FIPAMessage> successfulDeliveries = GamaListFactory.create(Types.get(IType.MESSAGE));
		final IList<FIPAMessage> failedDeliveries = GamaListFactory.create(Types.get(IType.MESSAGE));

		for (final FIPAMessage m : messagesForA) {
			final Conversation conv = m.getConversation();
			try {
				conv.addMessage(scope, m, a);
			} catch (final GamaRuntimeException e) {
				failedDeliveries.add(m);
				failureMessageInReplyTo(scope, m);
				conv.end();
				throw e;
			} finally {
				if (!failedDeliveries.contains(m)) {
					successfulDeliveries.add(m);
				}
			}
		}

		messagesToDeliver.remove(a);
		return successfulDeliveries;
	}

	/**
	 * @throws GamaRuntimeException
	 *             Deliver failure in reply to.
	 *
	 * @param m
	 *            the m
	 *
	 * @throws GamlException
	 *             the gaml exception
	 */
	protected FIPAMessage failureMessageInReplyTo(final IScope scope, final FIPAMessage m) throws GamaRuntimeException {
		if (m.getPerformative() == Performative.failure) { return null; }

		final FIPAMessage f = new FIPAMessage(scope);
		f.setSender(null);
		final IList<IAgent> receivers = GamaListFactory.create(Types.AGENT);
		receivers.add(m.getSender());
		f.setReceivers(receivers);
		f.setPerformative(Performative.failure);
		f.setConversation(m.getConversation());
		f.setContents(m.getContents(scope));
		return f;
	}

	/**
	 * Schedule for delivery.
	 *
	 * @param m
	 *            the m
	 */
	public void scheduleForDelivery(final IScope scope, final FIPAMessage m) {
		for (final IAgent a : m.getReceivers().iterable(scope)) {
			scheduleForDelivery(m.copy(scope), a);
		}
	}

	/**
	 * Schedule for delivery.
	 *
	 * @param m the m
	 * @param agent the agent
	 */
	private void scheduleForDelivery(final FIPAMessage m, final IAgent agent) {
		List<FIPAMessage> messages = messagesToDeliver.get(agent);
		if (messages == null) {
			messages = new ArrayList<>();
			messagesToDeliver.put(agent, messages);
		}
		messages.add(m);
	}

	/**
	 * @throws GamaRuntimeException
	 *             Schedule for delivery.
	 *
	 * @param m
	 *            the m
	 * @param protocol
	 *            the protocol name
	 *
	 * @throws GamlException
	 *             the gaml exception
	 */
	public void scheduleForDelivery(final IScope scope, final FIPAMessage m, final String protocol) {
		Conversation conv;
		conv = new Conversation(scope, protocol, m);
		m.setConversation(conv);
		scheduleForDelivery(scope, m);
	}

	/**
	 * Gets the single instance of MessageBroker.
	 *
	 * @param sim
	 *            the sim
	 *
	 * @return single instance of MessageBroker
	 */
	public static MessageBroker getInstance(final IScope scope) {
		MessageBroker instance = instances.get(scope.getSimulation());
		if (instance == null) {
			instance = new MessageBroker();
			instances.put(scope.getSimulation(), instance);

			scope.getSimulation().postEndAction(scope1 -> {
				instances.get(scope1.getSimulation()).manageConversationsAndMessages();
				return null;
			});
			scope.getSimulation().postDisposeAction(scope1 -> {
				if (instances.get(scope1.getSimulation()) != null) {
					instances.get(scope1.getSimulation()).schedulerDisposed();
				}
				instances.remove(scope1.getSimulation());
				return null;
			});
		}
		return instance;
	}

	/**
	 * Gets the messages for.
	 *
	 * @param agent the agent
	 * @return the messages for
	 */
	public IList<FIPAMessage> getMessagesFor(final IAgent agent) {
		if (!conversationsMessages.containsKey(agent)) { return GamaListFactory.EMPTY_LIST; }

		return conversationsMessages.get(agent).messages;
	}

	/**
	 * Gets the conversations for.
	 *
	 * @param agent the agent
	 * @return the conversations for
	 */
	public List<Conversation> getConversationsFor(final IAgent agent) {
		if (!conversationsMessages.containsKey(agent)) { return GamaListFactory.EMPTY_LIST; }

		return conversationsMessages.get(agent).conversations;
	}

	/**
	 * Adds the conversation.
	 *
	 * @param c the c
	 */
	public void addConversation(final Conversation c) {
		final IList<IAgent> members = GamaListFactory.create(Types.AGENT);
		members.add(c.getIntitiator());
		for (final IAgent m : c.getParticipants()) {
			members.add(m);
		}

		for (final IAgent m : members) {
			addConversation(m, c);
		}
	}

	/**
	 * Adds the conversation.
	 *
	 * @param a the a
	 * @param c the c
	 */
	private void addConversation(final IAgent a, final Conversation c) {
		ConversationsMessages cm = conversationsMessages.get(a);
		if (cm == null) {
			cm = new ConversationsMessages();
			conversationsMessages.put(a, cm);
		}

		cm.conversations.add(c);
	}

	/**
	 * @throws GamaRuntimeException
	 *             Removes the already ended conversations.
	 */
	public void manageConversationsAndMessages() throws GamaRuntimeException {

		// remove ended conversations
		List<Conversation> conversations;
		final List<Conversation> endedConversations = GamaListFactory.create(Types.get(ConversationType.CONV_ID));
		for (final IAgent a : conversationsMessages.keySet()) {
			if (a.dead()) {
				final ConversationsMessages cm = conversationsMessages.get(a);
				cm.conversations.clear();
				cm.messages.clear();
				cm.conversations = null;
				cm.messages = null;
				conversationsMessages.remove(a);
				return;
			}
			conversations = conversationsMessages.get(a).conversations;
			endedConversations.clear();

			for (final Conversation c : conversations) {
				if (c.isEnded() && c.areMessagesRead()) {
					endedConversations.add(c);
				}
			}

			for (final Conversation endedConv : endedConversations) {
				endedConv.dispose();
			}

			final List<FIPAMessage> alreadyReadMessages = GamaListFactory.create(Types.get(IType.MESSAGE));
			for (final FIPAMessage m : conversationsMessages.get(a).messages) {
				if (!m.isUnread()) {
					alreadyReadMessages.add(m);
				}
			}
			conversationsMessages.get(a).messages.removeAll(alreadyReadMessages);

			conversations.removeAll(endedConversations);
		}
	}

	/**
	 * The Class ConversationsMessages.
	 */
	class ConversationsMessages {

		/** The conversations. */
		IList<Conversation> conversations;

		/** The messages. */
		// agent mailbox : all un-read messages of an agent
		IList<FIPAMessage> messages;

		/**
		 * Instantiates a new conversations messages.
		 */
		ConversationsMessages() {
			this.conversations = GamaListFactory.create(Types.get(ConversationType.CONV_ID));
			this.messages = GamaListFactory.create(Types.get(IType.MESSAGE));
		}
	}

	/**
	 * Scheduler disposed.
	 */
	public void schedulerDisposed() {
		messagesToDeliver.clear();

		ConversationsMessages cm;
		for (final IAgent a : conversationsMessages.keySet()) {
			cm = conversationsMessages.get(a);
			cm.conversations.clear();
			cm.conversations = null;
			cm.messages.clear();
			cm.messages = null;
		}
		conversationsMessages.clear();
	}
}
