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
import com.bulletphysics.util.Stack;

/**
 * Cylinder shape around the X axis.
 * 
 * @author jezek2
 */
public class CylinderShapeX extends CylinderShape {

	public CylinderShapeX(Vector3 halfExtents) {
		super(halfExtents, false);
		upAxis = 0;
		recalcLocalAabb();
	}

	@Override
	public Vector3 localGetSupportingVertexWithoutMargin(Vector3 vec, Vector3 out) {
	    Stack stack = Stack.enter();
		Vector3 result = cylinderLocalSupportX(getHalfExtentsWithoutMargin(stack.allocVector3()), vec, out);
		stack.leave();
		return result;
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3[] vectors, Vector3[] supportVerticesOut, int numVectors) {
	    Stack stack = Stack.enter();
		for (int i = 0; i < numVectors; i++) {
			cylinderLocalSupportX(getHalfExtentsWithoutMargin(stack.allocVector3()), vectors[i], supportVerticesOut[i]);
		}
		stack.leave();
	}

	@Override
	public float getRadius() {
	    Stack stack = Stack.enter();
		float result = getHalfExtentsWithMargin(stack.allocVector3()).y;
		stack.leave();
		return result;
	}

	@Override
	public String getName() {
		return "CylinderX";
	}
	
}
