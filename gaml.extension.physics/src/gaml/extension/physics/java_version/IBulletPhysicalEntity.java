/*******************************************************************************************************
 *
 * IBulletPhysicalEntity.java, in simtools.gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.physics.java_version;

import javax.vecmath.Vector3f;

import gama.core.metamodel.shape.GamaPoint;
import gaml.extension.physics.common.IPhysicalEntity;
import gaml.extension.physics.common.VectorUtils;

/**
 * The Interface IBulletPhysicalEntity.
 */
public interface IBulletPhysicalEntity extends IPhysicalEntity<Vector3f> {
	
	/**
	 * To vector.
	 *
	 * @param v the v
	 * @return the vector 3 f
	 */
	@Override
	default Vector3f toVector(final GamaPoint v) {
		return VectorUtils.toBulletVector(v);
	}

	/**
	 * To gama point.
	 *
	 * @param v the v
	 * @return the gama point
	 */
	@Override
	default GamaPoint toGamaPoint(final Vector3f v) {
		return VectorUtils.toGamaPoint(v);

	}

	/**
	 * To gama point.
	 *
	 * @param v the v
	 * @param result the result
	 * @return the gama point
	 */
	default GamaPoint toGamaPoint(final Vector3f v, final GamaPoint result) {
		return VectorUtils.toGamaPoint(v, result);

	}

	/**
	 * To vector.
	 *
	 * @param v the v
	 * @param result the result
	 * @return the vector 3 f
	 */
	default Vector3f toVector(final GamaPoint v, final Vector3f result) {
		return VectorUtils.toBulletVector(v, result);
	}
}
