/*******************************************************************************************************
 *
 * DormandPrince853Solver.java, in ummisco.gaml.extensions.maths, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.maths.ode.utils.solver;

import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

import gama.core.util.IList;
import gama.core.util.IMap;

/**
 * The Class DormandPrince853Solver.
 */
public class DormandPrince853Solver extends Solver {

	/**
	 * Instantiates a new dormand prince 853 solver.
	 *
	 * @param minStep the min step
	 * @param maxStep the max step
	 * @param scalAbsoluteTolerance the scal absolute tolerance
	 * @param scalRelativeTolerance the scal relative tolerance
	 * @param integrated_val the integrated val
	 */
	public DormandPrince853Solver(final double minStep, final double maxStep, final double scalAbsoluteTolerance,
			final double scalRelativeTolerance, final IMap<String, IList<Double>> integrated_val) {
		super((minStep + maxStep) / 2,
				new DormandPrince853Integrator(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance),
				integrated_val);
	}

}