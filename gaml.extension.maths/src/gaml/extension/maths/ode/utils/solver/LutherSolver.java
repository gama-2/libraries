/*******************************************************************************************************
 *
 * LutherSolver.java, in ummisco.gaml.extensions.maths, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.maths.ode.utils.solver;

import java.util.List;


import org.apache.commons.math3.ode.nonstiff.LutherIntegrator;

import gama.core.util.IList;
import gama.core.util.IMap;

/**
 * The Class LutherSolver.
 */
public class LutherSolver extends Solver {

	/**
	 * Instantiates a new luther solver.
	 *
	 * @param step the step
	 * @param integrated_val the integrated val
	 */
	public LutherSolver(final double step, final IMap<String, IList<Double>> integrated_val) {
		super(step, new LutherIntegrator(step), integrated_val);
	}

}
