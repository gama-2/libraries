/*******************************************************************************************************
 *
 * ConnectorMessage.java, in ummisco.gama.network, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.network.common;

import gama.core.extensions.messaging.GamaMessage;
import gama.core.runtime.IScope;

/**
 * The Interface ConnectorMessage.
 */
public interface ConnectorMessage {
	
	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	public String getSender();
	
	/**
	 * Gets the receiver.
	 *
	 * @return the receiver
	 */
	public String getReceiver();
	
	/**
	 * Gets the plain contents.
	 *
	 * @return the plain contents
	 */
	public String getPlainContents();
	
	/**
	 * Checks if is plain message.
	 *
	 * @return true, if is plain message
	 */
	public boolean isPlainMessage();
	
	/**
	 * Checks if is command message.
	 *
	 * @return true, if is command message
	 */
	public boolean isCommandMessage();
	
	/**
	 * Gets the contents.
	 *
	 * @param scope the scope
	 * @return the contents
	 */
	public GamaMessage getContents(IScope scope);
}
