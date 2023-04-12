/*******************************************************************************************************
 *
 * Conversation.java, in msi.gaml.extensions.fipa, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.fipa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.annotations.precompiler.GamlAnnotations.getter;
import gama.core.annotations.precompiler.GamlAnnotations.variable;
import gama.core.annotations.precompiler.GamlAnnotations.vars;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaList;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gaml.core.types.IType;
import gaml.core.types.Types;

/**
 * This class represents the notion of a Conversation which is comprised of several Messages, the order of which follow
 * a certain interaction protocol (e.g. FIPA-Request, FIPA-Subscribe,...). The methods it provides allow retrieval of
 * information about the conversation.
 *
 * This is the base class of all the classes that implement the concrete interaction protocols. The protocol models are
 * defined in the corresponding sub-classes.
 */

@vars ({ @variable (
		name = Conversation.MESSAGES,
		type = IType.LIST,
		of = IType.MESSAGE,
		doc = { @doc ("Returns the list of messages that compose this conversation") }),
		@variable (
				name = Conversation.PROTOCOL,
				type = IType.STRING,
				doc = { @doc ("Returns the name of the protocol followed by the conversation") }),
		@variable (
				name = Conversation.INITIATOR,
				type = IType.AGENT,
				doc = { @doc ("Returns the agent that has initiated this conversation") }),
		@variable (
				name = Conversation.PARTICIPANTS,
				type = IType.LIST,
				of = IType.AGENT,
				doc = { @doc ("Returns the list of agents that participate to this conversation") }),
		@variable (
				name = Conversation.ENDED,
				type = IType.BOOL,
				init = "false",
				doc = { @doc ("Returns whether this conversation has ended or not") }) })
public class Conversation extends GamaList<FIPAMessage> {

	/** The protocol. */
	private FIPAProtocol protocol;

	/** The Constant PROTOCOL. */
	public final static String PROTOCOL = "protocol";
	
	/** The Constant INITIATOR. */
	public final static String INITIATOR = "initiator";
	
	/** The Constant PARTICIPANTS. */
	public final static String PARTICIPANTS = "participants";
	
	/** The Constant ENDED. */
	public final static String ENDED = "ended";
	
	/** The Constant MESSAGES. */
	public final static String MESSAGES = "messages";

	/** The owner of this conversation. */
	private IAgent initiator;

	/** Other Agent in the conversations. */
	private final IList<IAgent> participants = GamaListFactory.create(Types.AGENT);

	/** The protocol node participant map. */
	private final Map<IAgent, ProtocolNode> protocolNodeParticipantMap = new HashMap<>();

	/** The no protocol node participant map. */
	private final Map<IAgent, FIPAMessage> noProtocolNodeParticipantMap = new HashMap<>();

	/** The current node in the protocol tree. */
	// private ProtocolNode currentNode;
	/**
	 * Plays the role of a mailbox, contains all the messages sent by the other agent in this Conversation.
	 */
	private final IList<FIPAMessage> messages = GamaListFactory.create(Types.get(IType.MESSAGE));

	/** The ended. */
	private boolean ended = false;

	/**
	 * @throws GamaRuntimeException
	 *             Method to dynamically load a Conversation instance which follows the given protocol and belongs to
	 *             the given Agent.
	 *
	 * @param sim
	 *            the sim
	 * @param protocolName
	 *            the protocol name
	 * @param message
	 *            the message
	 *
	 * @return The appropriate instance of Conversation for the protocol given
	 *
	 * @throws GamaRuntimeException
	 *             the gaml exception
	 *
	 */
	protected Conversation(final IScope scope, final String protocolName, final FIPAMessage message)
			throws GamaRuntimeException {
		super(0, Types.get(IType.MESSAGE));
		if (protocolName == null) { throw GamaRuntimeException.error("Undefined protocol name", scope); }
		try {
			FIPAProtocol.Names p = FIPAProtocol.Names.valueOf(protocolName.replace('-', '_'));
			protocol = p.protocol;
		} catch (IllegalArgumentException e) {
			throw GamaRuntimeException.error("Unknown protocol: " + protocolName, scope);
		}

		initiator = message.getSender();

		participants.addAll(message.getReceivers());
		if (participants.isEmpty() || participants.contains(null)) {
			throw GamaRuntimeException.warning("The message : " + message.toString() + " has no receivers.", scope);
		}

		MessageBroker.getInstance(scope).addConversation(this);
	}

	/**
	 * Sets the protocol.
	 *
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(final FIPAProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * Adds a message to the conversation.
	 *
	 * @param message
	 *            The Message to be added
	 * @param receiver
	 *            The agent who receive the message
	 *
	 */
	protected void addMessage(final IScope scope, final FIPAMessage message, final IAgent receiver)
			throws GamaRuntimeException {

		// OutputManager.debug(name + " adds message " + message);

		// Check if the message belongs to this conversation
		final Conversation msgConv = message.getConversation();
		if (msgConv == null || msgConv != this) {
			throw GamaRuntimeException.warning("Conversation is invalid or not specified", scope);
		}

		if (protocol.hasProtocol()) {
			/** we use a protocol for this Conversation */
			// check the validity of this Message with the protocol model.
			// Raises an exception if the message is not valid.
			final boolean senderIsInitiator = message.getSender().equals(initiator);
			ProtocolNode currentNode;

			if (senderIsInitiator) {
				if (message.getReceivers().contains(receiver)) {
					if (protocolNodeParticipantMap.containsKey(receiver)) {
						currentNode = protocolNodeParticipantMap.remove(receiver);
						protocolNodeParticipantMap.put(receiver, protocol.getNode(scope, message, currentNode,
								message.getPerformative(), senderIsInitiator));
					} else {
						currentNode =
								protocol.getNode(scope, message, null, message.getPerformative(), senderIsInitiator);

						if (currentNode != null) {
							protocolNodeParticipantMap.put(receiver, currentNode);
						}
					}
				} else {
					throw GamaRuntimeException.warning(
							"Receiver " + receiver.getName() + " is not in the available message's receivers.", scope);
				}

			} else if (participants.contains(message.getSender())) {
				if (protocolNodeParticipantMap.containsKey(message.getSender())) {
					currentNode = protocolNodeParticipantMap.remove(message.getSender());
					protocolNodeParticipantMap.put(message.getSender(), protocol.getNode(scope, message, currentNode,
							message.getPerformative(), senderIsInitiator));
				} else {
					currentNode = protocol.getNode(scope, message, null, message.getPerformative(), senderIsInitiator);

					if (currentNode != null) {
						protocolNodeParticipantMap.put(message.getSender(), currentNode);
					}
				}
			}
		} else { // we use NoProtocol
			final boolean senderIsInitiator = message.getSender().equals(initiator);
			FIPAMessage currentMessage;

			if (senderIsInitiator) {
				currentMessage = noProtocolNodeParticipantMap.get(receiver);

				if (currentMessage != null && currentMessage.getPerformative() == Performative.end_conversation) {
					throw GamaRuntimeException.warning(
							"Message received in conversation which has already ended." + message + this, scope);
				}

				if (currentMessage != null) {
					noProtocolNodeParticipantMap.remove(receiver);
				}
				noProtocolNodeParticipantMap.put(receiver, message);

			} else if (participants.contains(message.getSender())) {
				currentMessage = noProtocolNodeParticipantMap.get(message.getSender());

				if (currentMessage != null && currentMessage.getPerformative() == Performative.end_conversation) {
					throw GamaRuntimeException.warning(
							"Message received in conversation which has already ended." + message + this, scope);
				}

				if (currentMessage != null) {
					noProtocolNodeParticipantMap.remove(message.getSender());
				}
				noProtocolNodeParticipantMap.put(message.getSender(), message);
			}
		}

		messages.add(message);
	}

	/**
	 * Retrieves all the messages kept by this role of the conversation. Note: There are two roles in a conversation :
	 * initiator and participant.
	 *
	 * @return the messages
	 */
	@getter (MESSAGES)
	public IList<FIPAMessage> getMessages() {
		return messages;
	}

	/**
	 * Gets the intitiator.
	 *
	 * @return the intitiator
	 */
	@getter (INITIATOR)
	public IAgent getIntitiator() {
		return initiator;
	}

	/**
	 * Gets the participants.
	 *
	 * @return the participants
	 */
	@getter (PARTICIPANTS)
	public IList<IAgent> getParticipants() {
		return participants;
	}

	/**
	 * Gets the protocol name.
	 *
	 * @return the protocol name
	 */
	@getter (PROTOCOL)
	public String getProtocolName() {
		if (protocol == null) { return null; }
		return protocol.getName();
	}

	/**
	 * Checks if is ended.
	 *
	 * @return true, if is ended
	 */
	@getter (ENDED)
	public boolean isEnded() {
		return ended || areAllNodeEnded();
	}

	/**
	 * Are messages read.
	 *
	 * @return true, if successful
	 */
	public boolean areMessagesRead() {
		for (final FIPAMessage m : messages) {
			if (m.isUnread()) { return false; }
		}

		return true;
	}

	/**
	 * Are all node ended.
	 *
	 * @return true, if successful
	 */
	private boolean areAllNodeEnded() {
		if (protocol != null && protocol.hasProtocol()) {
			final Collection<ProtocolNode> protocolNodes = protocolNodeParticipantMap.values();
			if (protocolNodes.isEmpty()) { return false; }
			for (final ProtocolNode node : protocolNodes) {
				if (!(node != null && node.isTerminal())) { return false; }
			}
			return true;
		}
		final Collection<FIPAMessage> finalMsgs = noProtocolNodeParticipantMap.values();
		if (finalMsgs.isEmpty()) { return false; }
		for (final FIPAMessage finalMsg : finalMsgs) {
			if (finalMsg.getPerformative() != Performative.end_conversation) { return false; }
		}
		return true;
	}

	/**
	 * End.
	 */
	protected void end() {
		ended = true;
	}

	/**
	 * Dispose.
	 */
	public synchronized void dispose() {
		end();
		protocolNodeParticipantMap.clear();
		noProtocolNodeParticipantMap.clear();
		participants.clear();
		initiator = null;
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return "Conversation between initiator: " + this.getIntitiator() + " and participants: "
				+ this.getParticipants();
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Conversation between initiator: " + this.getIntitiator() + " and participants: "
				+ this.getParticipants();
	}

	@Override
	public String toString() {
		return "Conversation between initiator: " + this.getIntitiator() + " and participants: "
				+ this.getParticipants();
	}
}
