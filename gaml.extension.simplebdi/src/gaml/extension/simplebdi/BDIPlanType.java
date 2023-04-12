/*******************************************************************************************************
 *
 * BDIPlanType.java, in msi.gaml.architecture.simplebdi, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.0).
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
import gama.core.serializer.gamaType.converter;
import gaml.core.types.GamaType;
import gaml.core.types.IType;

/**
 * The Class BDIPlanType.
 */
@SuppressWarnings ("unchecked")
@type (
		name = "BDIPlan",
		id = BDIPlanType.BDIPLANTYPE_ID,
		wraps = { BDIPlan.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("a type representing a plan for the BDI engine")
@converter (GamaBDIPlanConverter.class)
public class BDIPlanType extends GamaType<BDIPlan> {

	/** The Constant id. */
	public final static int BDIPLANTYPE_ID = IType.AVAILABLE_TYPES + 546655;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object into a BDIPlan if it is an instance of a BDIPlan")
	public BDIPlan cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof BDIPlan) return (BDIPlan) obj;
		/*
		 * if ( obj != null && obj instanceof Map ) { Map<String, Object> map = (Map<String, Object>) obj; String nm =
		 * (String) (map.containsKey("name") ? map.get("name") : "predicate"); Double pr = (Double)
		 * (map.containsKey("priority") ? map.get("priority") : 1.0); Map values = (Map) (map.containsKey("name") ?
		 * map.get("values") : null); return new Predicate(nm, pr, values); }
		 */
		return null;
	}

	@Override
	public BDIPlan getDefault() { return null; }

}
