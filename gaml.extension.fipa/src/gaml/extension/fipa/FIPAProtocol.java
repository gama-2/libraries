/*******************************************************************************************************
 *
 * FIPAProtocol.java, in msi.gaml.extensions.fipa, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.fipa;

import static gaml.extension.fipa.Performative.accept_proposal;
import static gaml.extension.fipa.Performative.agree;
import static gaml.extension.fipa.Performative.cancel;
import static gaml.extension.fipa.Performative.cfp;
import static gaml.extension.fipa.Performative.failure;
import static gaml.extension.fipa.Performative.inform;
import static gaml.extension.fipa.Performative.not_understood;
import static gaml.extension.fipa.Performative.propose;
import static gaml.extension.fipa.Performative.proxy;
import static gaml.extension.fipa.Performative.query;
import static gaml.extension.fipa.Performative.refuse;
import static gaml.extension.fipa.Performative.reject_proposal;
import static gaml.extension.fipa.Performative.request;
import static gaml.extension.fipa.Performative.request_when;
import static gaml.extension.fipa.Performative.subscribe;
import static java.util.Arrays.asList;
import static org.jgrapht.Graphs.addOutgoingEdges;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * The Class FIPAProtocol.
 *
 * @author drogoul
 */
abstract public class FIPAProtocol extends DefaultDirectedGraph<ProtocolNode, Object> {

	/**
	 * The Enum Names.
	 */
	public static enum Names {

		/** The fipa brokering. */
		fipa_brokering(new FIPAProtocol("fipa_brokering") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(proxy), iNode(cancel), pNode(refuse), pNode(agree));
				addTree(iNode(cancel), iNode(proxy), pNode(failure), pNode(inform));
				addTree(pNode(agree), iNode(cancel), pNode(failure), pNode(inform));
				return iNode(proxy);
			}
		}), 
 /** The fipa contract net. */
 fipa_contract_net(new FIPAProtocol("fipa_contract_net") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(cfp), pNode(failure), iNode(cancel), pNode(refuse), pNode(propose));
				addTree(pNode(propose), iNode(failure), iNode(cancel), iNode(accept_proposal), iNode(reject_proposal));
				addTree(iNode(cancel), pNode(failure), pNode(inform));
				addTree(iNode(accept_proposal), pNode(failure), pNode(inform));
				return iNode(cfp);
			}
		}), 
 /** The fipa iterated contract net. */
 fipa_iterated_contract_net(new FIPAProtocol("fipa_iterated_contract_net") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(cfp), pNode(failure), iNode(cancel), pNode(refuse), pNode(propose));
				addTree(pNode(propose), iNode(failure), iNode(cancel), iNode(accept_proposal), iNode(reject_proposal),
						iNode(cfp));
				addTree(iNode(cancel), pNode(failure), pNode(inform));
				addTree(iNode(accept_proposal), pNode(failure), pNode(inform));
				return iNode(cfp);
			}
		}), 
 /** The fipa propose. */
 fipa_propose(new FIPAProtocol("fipa_propose") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(propose), pNode(reject_proposal), pNode(accept_proposal), iNode(cancel));
				addTree(iNode(cancel), pNode(failure), pNode(inform));
				return iNode(propose);
			}
		}), 
 /** The fipa query. */
 fipa_query(new FIPAProtocol("fipa_query") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(query), pNode(refuse), pNode(agree), iNode(cancel));
				addTree(pNode(agree), pNode(inform), pNode(failure));
				addTree(iNode(cancel), pNode(inform), pNode(failure));
				return iNode(query);

			}
		}), 
 /** The fipa request. */
 fipa_request(new FIPAProtocol("fipa_request") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(request), pNode(not_understood), iNode(cancel), pNode(agree), pNode(refuse));
				addTree(iNode(cancel), pNode(failure), pNode(inform));
				addTree(pNode(agree), pNode(failure), pNode(inform));
				return iNode(request);
			}
		}), 
 /** The fipa request when. */
 fipa_request_when(new FIPAProtocol("fipa_request_when") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(request_when), iNode(cancel), pNode(refuse), pNode(agree));
				addTree(iNode(cancel), pNode(failure), pNode(inform));
				addTree(iNode(agree), pNode(failure), pNode(inform));
				return iNode(request_when);
			}
		}), 
 /** The fipa subscribe. */
 fipa_subscribe(new FIPAProtocol("fipa_subscribe") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				addTree(iNode(subscribe), pNode(refuse), pNode(agree), iNode(cancel));
				addTree(pNode(agree), pNode(inform), iNode(cancel), pNode(failure));
				addTree(pNode(inform), iNode(cancel), pNode(failure));
				addTree(iNode(cancel), pNode(inform), pNode(failure)); // ???
				return iNode(subscribe);
			}
		}), 
 /** The no protocol. */
 no_protocol(new FIPAProtocol("no_protocol") {
			@Override
			protected ProtocolNode populateProtocolGraph() {
				return null;
			}
		});

		/** The protocol. */
		FIPAProtocol protocol;

		/**
		 * Instantiates a new names.
		 *
		 * @param p the p
		 */
		Names(FIPAProtocol p) {
			protocol = p;
		}

	}

	/**
	 * Instantiates a new FIPA protocol.
	 *
	 * @param name the name
	 */
	public FIPAProtocol(String name) {
		super(Object.class);
		this.name = name;
		root = populateProtocolGraph();
	}

	/** The root. */
	private final ProtocolNode root;

	/** The name. */
	private String name;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Adds a subgraph (usually a tree) to the protocol. First parameter is the source node, others are the targets
	 *
	 * @param start
	 * @param nodes
	 */
	void addTree(ProtocolNode start, ProtocolNode... nodes) {
		addOutgoingEdges(this, start, asList(nodes));
	}

	/**
	 * Returns a node sent by a participant with this performative. If the node already exists, returns it. Otherwise,
	 * creates it and adds it to the protocol nodes
	 *
	 * @param performative
	 * @return
	 */
	ProtocolNode pNode(Performative performative) {
		for (ProtocolNode node : this.vertexSet()) {
			if (node.getPerformative() == performative && !node.isSentByInitiator()) { return node; }
		}
		ProtocolNode node = new ProtocolNode(this, performative, false);
		addVertex(node);
		return node;
	}

	/**
	 * Returns a node sent by the initiator with this performative. If the node already exists, returns it. Otherwise,
	 * creates it and adds it to the protocol nodes
	 *
	 * @param performative
	 * @return
	 */
	ProtocolNode iNode(Performative performative) {
		for (ProtocolNode node : this.vertexSet()) {
			if (node.getPerformative() == performative && node.isSentByInitiator()) { return node; }
		}
		ProtocolNode node = new ProtocolNode(this, performative, true);
		addVertex(node);
		return node;
	}

	/**
	 * Populate protocol graph.
	 *
	 * @return the protocol node
	 */
	protected abstract ProtocolNode populateProtocolGraph();

	/**
	 * Checks for protocol.
	 *
	 * @return true if a protocol tree is defined, false otherwise.
	 */
	public final boolean hasProtocol() {
		return root != null;
	}

	/**
	 * Gets the node corresponding to a performative after the current node of the protocol.
	 */
	protected ProtocolNode getNode(final IScope scope, final FIPAMessage message, final ProtocolNode currentNode,
			final Performative performative, final boolean initiator) throws GamaRuntimeException {
		if (currentNode == null) {
			if (root != null && root.getPerformative() == performative)
				return root;
			return null;
		}
		final List<ProtocolNode> followingNodes = Graphs.successorListOf(this, currentNode);
		if (followingNodes.size() == 0) {
			throw GamaRuntimeException.warning("Message received in a conversation which has already ended!", scope);
		}
		final List<ProtocolNode> potentialMatchingNodes = new ArrayList<>();
		for (final ProtocolNode followingNode : followingNodes) {
			if (performative == followingNode.getPerformative()) {
				potentialMatchingNodes.add(followingNode);
			}
		}
		if (potentialMatchingNodes.isEmpty()) {
			throw GamaRuntimeException.warning("Protocol : " + this.getName()
					+ ". Unexpected message received of performative : " + message.getPerformativeName(), scope);
		}
		ProtocolNode matchingNode = null;
		for (final ProtocolNode potentialMatchingNode : potentialMatchingNodes) {
			if (initiator == potentialMatchingNode.isSentByInitiator()) {
				matchingNode = potentialMatchingNode;
				break;
			}
		}

		if (matchingNode == null) {
			throw GamaRuntimeException.warning("Couldn't match expected message types and participant", scope);
		}
		return matchingNode;

	}
}
