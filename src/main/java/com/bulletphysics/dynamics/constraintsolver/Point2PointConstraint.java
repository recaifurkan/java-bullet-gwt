/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysics.dynamics.constraintsolver;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.Stack;

/** Point to point constraint between two rigid bodies each with a pivot point that descibes the "ballsocket" location in local
 * space.
 * 
 * @author jezek2 */
public class Point2PointConstraint extends TypedConstraint {

	private final JacobianEntry[] jac = new JacobianEntry[]/* [3] */ {new JacobianEntry(), new JacobianEntry(),
		new JacobianEntry()}; // 3 orthogonal linear constraints

	private final Vector3 pivotInA = new Vector3();
	private final Vector3 pivotInB = new Vector3();

	public ConstraintSetting setting = new ConstraintSetting();

	public Point2PointConstraint () {
		super(TypedConstraintType.POINT2POINT_CONSTRAINT_TYPE);
	}

	public Point2PointConstraint (RigidBody rbA, RigidBody rbB, Vector3 pivotInA, Vector3 pivotInB) {
		super(TypedConstraintType.POINT2POINT_CONSTRAINT_TYPE, rbA, rbB);
		this.pivotInA.set(pivotInA);
		this.pivotInB.set(pivotInB);
	}

	public Point2PointConstraint (RigidBody rbA, Vector3 pivotInA) {
		super(TypedConstraintType.POINT2POINT_CONSTRAINT_TYPE, rbA);
		this.pivotInA.set(pivotInA);
		this.pivotInB.set(pivotInA);
		Stack stack = Stack.enter();
		rbA.getCenterOfMassTransform(stack.allocTransform()).transform(this.pivotInB);
		stack.leave();
	}

	@Override
	public void buildJacobian () {
		appliedImpulse = 0f;
		Stack stack = Stack.enter();

		Vector3 normal = stack.allocVector3();
		normal.set(0f, 0f, 0f);

		Matrix3 tmpMat1 = stack.allocMatrix3();
		Matrix3 tmpMat2 = stack.allocMatrix3();
		Vector3 tmp1 = stack.allocVector3();
		Vector3 tmp2 = stack.allocVector3();
		Vector3 tmpVec = stack.allocVector3();

		Transform centerOfMassA = rbA.getCenterOfMassTransform(stack.allocTransform());
		Transform centerOfMassB = rbB.getCenterOfMassTransform(stack.allocTransform());

		for (int i = 0; i < 3; i++) {
			VectorUtil.setCoord(normal, i, 1f);

			tmpMat1.set(centerOfMassA.basis).transpose();
			tmpMat2.set(centerOfMassB.basis).transpose();

			tmp1.set(pivotInA);
			centerOfMassA.transform(tmp1);
			tmp1.sub(rbA.getCenterOfMassPosition(tmpVec));

			tmp2.set(pivotInB);
			centerOfMassB.transform(tmp2);
			tmp2.sub(rbB.getCenterOfMassPosition(tmpVec));

			jac[i].init(tmpMat1, tmpMat2, tmp1, tmp2, normal, rbA.getInvInertiaDiagLocal(stack.allocVector3()), rbA.getInvMass(),
				rbB.getInvInertiaDiagLocal(stack.allocVector3()), rbB.getInvMass());
			VectorUtil.setCoord(normal, i, 0f);
		}
		stack.leave();
	}

	@Override
	public void solveConstraint (float timeStep) {
		Stack stack = Stack.enter();
		Vector3 tmp = stack.allocVector3();
		Vector3 tmp2 = stack.allocVector3();
		Vector3 tmpVec = stack.allocVector3();

		Transform centerOfMassA = rbA.getCenterOfMassTransform(stack.allocTransform());
		Transform centerOfMassB = rbB.getCenterOfMassTransform(stack.allocTransform());

		Vector3 pivotAInW = stack.alloc(pivotInA);
		centerOfMassA.transform(pivotAInW);

		Vector3 pivotBInW = stack.alloc(pivotInB);
		centerOfMassB.transform(pivotBInW);

		Vector3 normal = stack.allocVector3();
		normal.set(0f, 0f, 0f);

		// btVector3 angvelA = m_rbA.getCenterOfMassTransform().getBasis().transpose() * m_rbA.getAngularVelocity();
		// btVector3 angvelB = m_rbB.getCenterOfMassTransform().getBasis().transpose() * m_rbB.getAngularVelocity();

		for (int i = 0; i < 3; i++) {
			VectorUtil.setCoord(normal, i, 1f);
			float jacDiagABInv = 1f / jac[i].getDiagonal();

			Vector3 rel_pos1 = stack.allocVector3();
			rel_pos1.set(pivotAInW).sub(rbA.getCenterOfMassPosition(tmpVec));
			Vector3 rel_pos2 = stack.allocVector3();
			rel_pos2.set(pivotBInW).sub(rbB.getCenterOfMassPosition(tmpVec));
			// this jacobian entry could be re-used for all iterations

			Vector3 vel1 = rbA.getVelocityInLocalPoint(rel_pos1, stack.allocVector3());
			Vector3 vel2 = rbB.getVelocityInLocalPoint(rel_pos2, stack.allocVector3());
			Vector3 vel = stack.allocVector3();
			vel.set(vel1).sub(vel2);

			float rel_vel;
			rel_vel = normal.dot(vel);

			/*
			 * //velocity error (first order error) btScalar rel_vel =
			 * m_jac[i].getRelativeVelocity(m_rbA.getLinearVelocity(),angvelA, m_rbB.getLinearVelocity(),angvelB);
			 */

			// positional error (zeroth order error)
			tmp.set(pivotAInW).sub(pivotBInW);
			float depth = -tmp.dot(normal); // this is the error projected on the normal

			float impulse = depth * setting.tau / timeStep * jacDiagABInv - setting.damping * rel_vel * jacDiagABInv;

			float impulseClamp = setting.impulseClamp;
			if (impulseClamp > 0f) {
				if (impulse < -impulseClamp) {
					impulse = -impulseClamp;
				}
				if (impulse > impulseClamp) {
					impulse = impulseClamp;
				}
			}

			appliedImpulse += impulse;
			Vector3 impulse_vector = stack.allocVector3();
			impulse_vector.set(normal).scl(impulse);
			tmp.set(pivotAInW).sub(rbA.getCenterOfMassPosition(tmpVec));
			rbA.applyImpulse(impulse_vector, tmp);
			tmp.set(impulse_vector).scl(-1);
			tmp2.set(pivotBInW).sub(rbB.getCenterOfMassPosition(tmpVec));
			rbB.applyImpulse(tmp, tmp2);

			VectorUtil.setCoord(normal, i, 0f);
		}
		stack.leave();
	}

	public void updateRHS (float timeStep) {
	}

	public void setPivotA (Vector3 pivotA) {
		pivotInA.set(pivotA);
	}

	public void setPivotB (Vector3 pivotB) {
		pivotInB.set(pivotB);
	}

	public Vector3 getPivotInA (Vector3 out) {
		out.set(pivotInA);
		return out;
	}

	public Vector3 getPivotInB (Vector3 out) {
		out.set(pivotInB);
		return out;
	}

	////////////////////////////////////////////////////////////////////////////

	public static class ConstraintSetting {
		public float tau = 0.3f;
		public float damping = 1f;
		public float impulseClamp = 0f;
	}

}
