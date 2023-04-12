/*******************************************************************************************************
 *
 * MathConstantSupplier.java, in ummisco.gaml.extensions.maths, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.maths.ode;

import gaml.core.constants.IConstantAcceptor;
import gaml.core.constants.IConstantsSupplier;

/**
 * The Class MathConstantSupplier.
 */
public class MathConstantSupplier implements IConstantsSupplier {

	@Override
	public void supplyConstantsTo(final IConstantAcceptor acceptor) {
		browse(MathConstants.class, acceptor);
	}

}
