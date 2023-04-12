/*******************************************************************************************************
 *
 * NormType.java, in msi.gaml.architecture.simplebdi, is part of the source code of the
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
 * The Class NormType.
 */
@type(name = "Norm", id = NormType.id, wraps = { Norm.class }, concept = { IConcept.TYPE, IConcept.BDI })
@doc("represents a norm")
public class NormType extends GamaType<Norm>{

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 546660;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc("cast an object into a norm, if it is an instance of a norm")
	public Norm cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof Norm) {
			return (Norm) obj;
		}
		return null;
	}

	@Override
	public Norm getDefault() {
		return null;
	}
	
}
