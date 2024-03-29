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

package com.bulletphysics.collision.shapes;

import com.badlogic.gdx.math.Vector3;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.Stack;
import com.java.lang.InternalError;

/**
 * ConvexInternalShape is an internal base class, shared by most convex shape implementations.
 * 
 * @author jezek2
 */
public abstract class ConvexInternalShape extends ConvexShape {

	// local scaling. collisionMargin is not scaled !
	protected final Vector3 localScaling = new Vector3(1f, 1f, 1f);
	protected final Vector3 implicitShapeDimensions = new Vector3();
	protected float collisionMargin = BulletGlobals.CONVEX_DISTANCE_MARGIN;

	/**
	 * getAabb's default implementation is brute force, expected derived classes to implement a fast dedicated version.
	 */
	@Override
	public void getAabb(Transform t, Vector3 aabbMin, Vector3 aabbMax) {
		getAabbSlow(t, aabbMin, aabbMax);
	}
	
	@Override
	public void getAabbSlow(Transform trans, Vector3 minAabb, Vector3 maxAabb) {
	    Stack stack = Stack.enter();
		float margin = getMargin();
		Vector3 vec = stack.allocVector3();
		Vector3 tmp1 = stack.allocVector3();
		Vector3 tmp2 = stack.allocVector3();
		
		for (int i=0;i<3;i++)
		{
			vec.set(0f, 0f, 0f);
			VectorUtil.setCoord(vec, i, 1f);

			MatrixUtil.transposeTransform(tmp1, vec, trans.basis);
			localGetSupportingVertex(tmp1, tmp2);
			
			trans.transform(tmp2);

			VectorUtil.setCoord(maxAabb, i, VectorUtil.getCoord(tmp2, i) + margin);

			VectorUtil.setCoord(vec, i, -1f);

			MatrixUtil.transposeTransform(tmp1, vec, trans.basis);
			localGetSupportingVertex(tmp1, tmp2);
			trans.transform(tmp2);

			VectorUtil.setCoord(minAabb, i, VectorUtil.getCoord(tmp2, i) - margin);
		}
		stack.leave();
	}

	@Override
	public Vector3 localGetSupportingVertex(Vector3 vec, Vector3 out) {
		Vector3 supVertex = localGetSupportingVertexWithoutMargin(vec, out);

		if (getMargin() != 0f) {
		    Stack stack = Stack.enter();
			Vector3 vecnorm = stack.alloc(vec);
			if (vecnorm.len2() < (BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON)) {
				vecnorm.set(-1f, -1f, -1f);
			}
			vecnorm.nor();
			vecnorm.scl(getMargin());
			supVertex.add(vecnorm);
			stack.leave();
		}
		return out;
	}
	
	public void setLocalScaling(Vector3 scaling) {
		VectorUtil.absolute(localScaling.set(scaling));
	}
	
	public Vector3 getLocalScaling(Vector3 out) {
		out.set(localScaling);
		return out;
	}

	public float getMargin() {
		return collisionMargin;
	}

	public void setMargin(float margin) {
		this.collisionMargin = margin;
	}

	@Override
	public int getNumPreferredPenetrationDirections() {
		return 0;
	}

	@Override
	public void getPreferredPenetrationDirection(int index, Vector3 penetrationVector) {
		throw new InternalError();
	}
	
}
