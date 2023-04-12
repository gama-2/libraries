/*******************************************************************************************************
 *
 * SimpleBdiArchitectureParallel.java, in msi.gaml.architecture.simplebdi, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.simplebdi;

import java.util.List;

import gama.core.annotations.precompiler.IConcept;
import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.annotations.precompiler.GamlAnnotations.skill;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gaml.core.descriptions.ConstantExpressionDescription;
import gaml.core.descriptions.IDescription;
import gaml.core.expressions.IExpression;
import gaml.core.operators.Cast;
import gaml.core.operators.Maths;
import gaml.core.statements.AbstractStatement;
import gaml.core.statements.IStatement;

/**
 * The Class SimpleBdiArchitectureParallel.
 */
@skill (
		name = SimpleBdiArchitectureParallel.PARALLEL_BDI,
		concept = { IConcept.BDI, IConcept.ARCHITECTURE })
@doc ("compute the bdi architecture in parallel. This skill inherit all actions and variables from SimpleBdiArchitecture")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class SimpleBdiArchitectureParallel extends SimpleBdiArchitecture {

	/** The Constant PARALLEL_BDI. */
	public static final String PARALLEL_BDI = "parallel_bdi";
	
	/** The parallel. */
	IExpression parallel = ConstantExpressionDescription.TRUE_EXPR_DESCRIPTION;

	/**
	 * The Class UpdateEmotions.
	 */
	public class UpdateEmotions extends AbstractStatement {

		/**
		 * Instantiates a new update emotions.
		 *
		 * @param desc the desc
		 */
		public UpdateEmotions(IDescription desc) {
			super(desc);
		}

		@Override
		protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
			// computeEmotions(scope);
			return null;
		}

	}

	/**
	 * The Class UpdateSocialLinks.
	 */
	public class UpdateSocialLinks extends AbstractStatement {

		/**
		 * Instantiates a new update social links.
		 *
		 * @param desc the desc
		 */
		public UpdateSocialLinks(IDescription desc) {
			super(desc);
		}

		@Override
		protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
			updateSocialLinks(scope);
			return null;
		}

	}

	/**
	 * The Class UpdateEmotionsIntensity.
	 */
	public class UpdateEmotionsIntensity extends AbstractStatement {

		/**
		 * Instantiates a new update emotions intensity.
		 *
		 * @param desc the desc
		 */
		public UpdateEmotionsIntensity(IDescription desc) {
			super(desc);
		}

		@Override
		protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
			updateEmotionsIntensity(scope);
			return null;
		}

	}

	/**
	 * The Class UpdateLifeTimePredicates.
	 */
	public class UpdateLifeTimePredicates extends AbstractStatement {

		/**
		 * Instantiates a new update life time predicates.
		 *
		 * @param desc the desc
		 */
		public UpdateLifeTimePredicates(IDescription desc) {
			super(desc);
		}

		@Override
		protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
			updateLifeTimePredicates(scope);
			return null;
		}

	}

	@Override
	public void preStep(final IScope scope, IPopulation<? extends IAgent> gamaPopulation) {
		final IExpression schedule = gamaPopulation.getSpecies().getSchedule();
		final List<? extends IAgent> agents =
				schedule == null ? gamaPopulation : Cast.asList(scope, schedule.value(scope));

		GamaExecutorService.execute(scope, new UpdateLifeTimePredicates(null), agents, parallel);
		GamaExecutorService.execute(scope, new UpdateEmotionsIntensity(null), agents, parallel);

		if (_reflexes != null)
			for (final IStatement r : _reflexes) {
				if (!scope.interrupted()) {
					GamaExecutorService.execute(scope, r, agents, ConstantExpressionDescription.FALSE_EXPR_DESCRIPTION);
				}
			}

		if (_perceptionNumber > 0) {
			for (int i = 0; i < _perceptionNumber; i++) {
				if (!scope.interrupted()) {
					PerceiveStatement statement = _perceptions.get(i);
					IExpression par = statement.getParallel() == null ? parallel : statement.getParallel();
					GamaExecutorService.execute(scope, statement, agents, par);
				}
			}
		}
		if (_rulesNumber > 0) {
			for (int i = 0; i < _rulesNumber; i++) {
				RuleStatement statement = _rules.get(i);
				IExpression par = statement.getParallel() == null ? parallel : statement.getParallel();
				GamaExecutorService.execute(scope, statement, agents, par);
			}
		}

		if (_lawsNumber > 0) {
			for (int i = 0; i < _lawsNumber; i++) {
				LawStatement statement = _laws.get(i);
				IExpression par = statement.getParallel() == null ? parallel : statement.getParallel();
				GamaExecutorService.execute(scope, statement, agents, par);
			}
		}

		// GamaExecutorService.execute(scope, new UpdateEmotions(null), agents,parallel) ;
		GamaExecutorService.execute(scope, new UpdateSocialLinks(null), agents, parallel);
		if (_copingNumber > 0) {
			for (int i = 0; i < _copingNumber; i++) {
				CopingStatement statement = _coping.get(i);
				IExpression par = statement.getParallel() == null ? parallel : statement.getParallel();
				GamaExecutorService.execute(scope, statement, agents, par);
			}
		}
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		final Boolean use_personality = scope.hasArg(USE_PERSONALITY) ? scope.getBoolArg(USE_PERSONALITY)
				: (Boolean) scope.getAgent().getAttribute(USE_PERSONALITY);
		if (use_personality) {
			Double expressivity = (Double) scope.getAgent().getAttribute(EXTRAVERSION);
			Double neurotisme = (Double) scope.getAgent().getAttribute(NEUROTISM);
			Double conscience = (Double) scope.getAgent().getAttribute(CONSCIENTIOUSNESS);
			Double agreeableness = (Double) scope.getAgent().getAttribute(AGREEABLENESS);
			scope.getAgent().setAttribute(CHARISMA, expressivity);
			scope.getAgent().setAttribute(RECEPTIVITY, 1 - neurotisme);
			scope.getAgent().setAttribute(PERSISTENCE_COEFFICIENT_PLANS, Maths.sqrt(scope, conscience));
			scope.getAgent().setAttribute(PERSISTENCE_COEFFICIENT_INTENTIONS, Maths.sqrt(scope, conscience));
			scope.getAgent().setAttribute(OBEDIENCE, Maths.sqrt(scope, (conscience + agreeableness) * 0.5));
		}
		// return executePlans(scope);
		Object result = executePlans(scope);
		if (!scope.getAgent().dead()) {
			// Activer la violation des normes
			updateNormViolation(scope);
			// Mettre à jour le temps de vie des normes
			updateNormLifetime(scope);

			// Part that manage the lifetime of predicates
			// if(result!=null){
			// updateLifeTimePredicates(scope);
			// updateEmotionsIntensity(scope);
			// }
		}
		return result;
	}

}
