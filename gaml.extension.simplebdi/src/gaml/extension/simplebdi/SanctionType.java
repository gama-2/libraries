/*******************************************************************************************************
 *
 * SanctionType.java, in msi.gaml.architecture.simplebdi, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.simplebdi;

import gama.core.annotations.precompiler.IConcept;
import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gaml.core.types.GamaType;
import gaml.core.types.IType;

/**
 * The Class SanctionType.
 */
@type(name = "Sanction", id = SanctionType.id, wraps = { Sanction.class }, concept = { IConcept.TYPE, IConcept.BDI })
@doc("represents a sanction")
public class SanctionType extends GamaType<Sanction>{

/** The Constant id. */
//
	public final static int id = IType.AVAILABLE_TYPES + 546661;
	
	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc("cast an object as a sanction, if it is an instance of a sanction")
	public Sanction cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof Sanction) {
			return (Sanction) obj;
		}
		return null;
	}

	@Override
	public Sanction getDefault() {
		return null;
	}

}
