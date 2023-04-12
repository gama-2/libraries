/*******************************************************************************************************
 *
 * Box2DBodyWrapper.java, in simtools.gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.physics.box2d_version;

import static gaml.core.types.GamaGeometryType.buildRectangle;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gaml.extension.physics.common.AbstractBodyWrapper;
import gaml.extension.physics.common.IBody;

/**
 * The Class Box2DBodyWrapper.
 */
public class Box2DBodyWrapper extends AbstractBodyWrapper<World, Body, Shape, Vec2> implements IBox2DPhysicalEntity {

	/** The def. */
	BodyDef def;
	
	/** The fixture def. */
	FixtureDef fixtureDef;
	
	/** The ms. */
	MassData ms;

	/**
	 * Instantiates a new box 2 D body wrapper.
	 *
	 * @param agent the agent
	 * @param world the world
	 */
	public Box2DBodyWrapper(final IAgent agent, final Box2DPhysicalWorld world) {
		super(agent, world);
	}

	@Override
	public Body createAndInitializeBody(final Shape shape, final World world) {
		def = new BodyDef();
		fixtureDef = new FixtureDef();
		ms = new MassData();
		IBody previous = (IBody) agent.getAttribute(BODY);
		if (previous != null) {
			GamaPoint pointTransfer = new GamaPoint();
			def.type = isStatic ? BodyType.STATIC : BodyType.DYNAMIC;
			def.angularDamping = previous.getAngularDamping();
			def.angularVelocity = (float) previous.getAngularVelocity(pointTransfer).norm();
			def.linearDamping = previous.getLinearDamping();
			toVector(previous.getLinearVelocity(pointTransfer), def.linearVelocity);
			def.allowSleep = false;
			def.userData = this;
		}
		Body body = world.createBody(def);
		if (previous != null) {
			fixtureDef.density = 1f;
			fixtureDef.friction = previous.getFriction();
			fixtureDef.restitution = previous.getRestitution();
			fixtureDef.shape = shape;
		}
		body.createFixture(fixtureDef);
		if (previous != null) { ms.mass = previous.getMass(); }
		body.setMassData(ms);
		return body;
	}

	@Override
	public float getMass() {
		return body.m_mass;
	}

	@Override
	public float getFriction() {
		return body.getFixtureList().getFriction();
	}

	@Override
	public float getRestitution() {
		return body.getFixtureList().getRestitution();
	}

	@Override
	public float getLinearDamping() {
		return body.m_linearDamping;
	}

	@Override
	public float getAngularDamping() {
		return body.m_angularDamping;
	}

	@Override
	public float getContactDamping() {
		// Doesnt exist
		return 0;
	}

	@Override
	public GamaPoint getAngularVelocity(final GamaPoint v) {
		v.setLocation(0, 0, body.getAngularVelocity());
		return v;
	}

	@Override
	public GamaPoint getLinearVelocity(final GamaPoint v) {
		return toGamaPoint(body.getLinearVelocity(), v);
	}

	@Override
	public IShape getAABB() {
		AABB aabb = body.getFixtureList().getAABB(0);
		Vec2 v = aabb.getExtents();
		return buildRectangle(v.x * 2, v.y * 2, new GamaPoint(v.x, v.y));
	}

	@Override
	public void setMass(final Double mass) {
		ms.mass = mass.floatValue();
		body.setMassData(ms);

	}

	@Override
	public void setCCD(final boolean v) {
		// Verify this
		body.setBullet(v);
	}

	@Override
	public void setFriction(final Double friction) {
		body.getFixtureList().setFriction(friction.floatValue());

	}

	@Override
	public void setRestitution(final Double restitution) {
		body.getFixtureList().setRestitution(restitution.floatValue());
	}

	@Override
	public void setDamping(final Double damping) {
		body.setLinearDamping(damping.floatValue());
	}

	@Override
	public void setAngularDamping(final Double damping) {
		body.setAngularDamping(damping.floatValue());
	}

	@Override
	public void setContactDamping(final Double damping) {
		// Not available
	}

	@Override
	public void setAngularVelocity(final GamaPoint angularVelocity) {
		body.setAngularVelocity((float) angularVelocity.z);
	}

	@Override
	public void setLinearVelocity(final GamaPoint linearVelocity) {
		body.setLinearVelocity(toVector(linearVelocity));
	}

	@Override
	public void setLocation(final GamaPoint loc) {
		body.setTransform(toVector(loc), body.getAngle());
	}

	@Override
	public void clearForces() {
		body.setLinearVelocity(new Vec2(0, 0));
		body.setAngularVelocity(0);
	}

	@Override
	public void applyImpulse(final GamaPoint impulse) {
		body.applyLinearImpulse(toVector(impulse), body.getLocalCenter(), true);
	}

	@Override
	public void applyTorque(final GamaPoint torque) {
		body.applyTorque((float) torque.norm());
	}

	@Override
	public void applyForce(final GamaPoint force) {
		body.applyForceToCenter(toVector(force));

	}

}
