/*******************************************************************************************************
 *
 * IBox2DPhysicalEntity.java, in simtools.gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.physics.box2d_version;

import org.jbox2d.common.Vec2;

import gama.core.metamodel.shape.GamaPoint;
import gaml.extension.physics.common.IPhysicalEntity;
import gaml.extension.physics.common.VectorUtils;

/**
 * The Interface IBox2DPhysicalEntity.
 */
public interface IBox2DPhysicalEntity extends IPhysicalEntity<Vec2> {
	
	/**
	 * To vector.
	 *
	 * @param v the v
	 * @return the vec 2
	 */
	@Override
	default Vec2 toVector(final GamaPoint v) {
		return VectorUtils.toBox2DVector(v);
	}

	/**
	 * To vector.
	 *
	 * @param v the v
	 * @param to the to
	 * @return the vec 2
	 */
	default Vec2 toVector(final GamaPoint v, final Vec2 to) {
		return VectorUtils.toBox2DVector(v, to);
	}

	/**
	 * To gama point.
	 *
	 * @param v the v
	 * @return the gama point
	 */
	@Override
	default GamaPoint toGamaPoint(final Vec2 v) {
		return VectorUtils.toGamaPoint(v);

	}

	/**
	 * To gama point.
	 *
	 * @param v the v
	 * @param result the result
	 * @return the gama point
	 */
	default GamaPoint toGamaPoint(final Vec2 v, final GamaPoint result) {
		return VectorUtils.toGamaPoint(v, result);
	}

}
