/*******************************************************************************************************
 *
 * ConversationType.java, in msi.gaml.extensions.fipa, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.0).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.fipa;

import gama.core.annotations.precompiler.IConcept;
import gama.core.annotations.precompiler.ISymbolKind;
import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gaml.core.types.GamaContainerType;
import gaml.core.types.IType;
import gaml.core.types.Types;

/**
 * The Class ConversationType.
 */
@type (
		name = ConversationType.CONVERSATION_STR,
		id = ConversationType.CONV_ID,
		wraps = { Conversation.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.FIPA },
		doc = @doc ("Represents a list of the messages exchanged by agents"))
public class ConversationType extends GamaContainerType<Conversation> {

	/** The Constant CONVERSATION_STR. */
	public final static String CONVERSATION_STR = "conversation";

	/** The Constant CONV_ID. */
	public final static int CONV_ID = 98;

	/**
	 * Instantiates a new conversation type.
	 */
	public ConversationType() {}

	@Override

	@doc ("Converts the operand into a conversation (if applicable) or retrieves the conversation of the message passed. Otherwise returns nil")
	public Conversation cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		return staticCast(scope, obj, param);
	}

	@Override
	public IType<FIPAMessage> getContentType() { return Types.get(FIPAMessage.class); }

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param object
	 *            the object
	 * @return the conversation
	 */
	public static Conversation staticCast(final IScope scope, final Object val, final Object object) {
		if (val instanceof Conversation) return (Conversation) val;
		if (val instanceof FIPAMessage) return ((FIPAMessage) val).getConversation();
		// ???
		return null;
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}
}
