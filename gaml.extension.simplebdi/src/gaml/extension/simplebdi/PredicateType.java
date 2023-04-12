/*******************************************************************************************************
 *
 * PredicateType.java, in msi.gaml.architecture.simplebdi, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.simplebdi;

import java.util.Map;

import gama.core.annotations.precompiler.IConcept;
import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gaml.core.types.GamaType;
import gaml.core.types.IType;

/**
 * The Class PredicateType.
 */
@SuppressWarnings ("unchecked")
@type (
		name = "predicate",
		id = PredicateType.id,
		wraps = { Predicate.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("represents a predicate")
public class PredicateType extends GamaType<Predicate> {

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 546654;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@SuppressWarnings ({ "rawtypes" })
	@Override
	@doc ("cast an object as a predicate")
	public Predicate cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Predicate) return (Predicate) obj;
		if (obj instanceof String) return new Predicate((String) obj);
		if (obj instanceof Map) {
			final Map<String, Object> map = (Map<String, Object>) obj;
			final String nm = (String) (map.containsKey("name") ? map.get("name") : "predicate");
			final IMap values = (IMap) (map.containsKey("name") ? map.get("values") : null);
			return new Predicate(nm, values);
		}
		return null;
	}

	@Override
	public Predicate getDefault() { return null; }

}
