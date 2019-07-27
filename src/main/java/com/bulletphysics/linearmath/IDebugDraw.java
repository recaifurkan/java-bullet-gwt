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

package com.bulletphysics.linearmath;

import com.badlogic.gdx.math.Vector3;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.util.Stack;

/**
 * BDebugDraw interface class allows hooking up a debug renderer to visually debug
 * simulations.<p>
 * 
 * Typical use case: create a debug drawer object, and assign it to a {@link CollisionWorld}
 * or {@link DynamicsWorld} using setDebugDrawer and call debugDrawWorld.<p>
 * 
 * A class that implements the BDebugDraw interface has to implement the drawLine
 * method at a minimum.
 * 
 * @author jezek2
 */
public abstract class IDebugDraw {
	
	//protected final BulletStack stack = BulletStack.get();

	public abstract void drawLine(Vector3 from, Vector3 to, Vector3 color);
	
	public void drawTriangle(Vector3 v0, Vector3 v1, Vector3 v2, Vector3 n0, Vector3 n1, Vector3 n2, Vector3 color, float alpha) {
		drawTriangle(v0, v1, v2, color, alpha);
	}
	
	public void drawTriangle(Vector3 v0, Vector3 v1, Vector3 v2, Vector3 color, float alpha) {
		drawLine(v0, v1, color);
		drawLine(v1, v2, color);
		drawLine(v2, v0, color);
	}

	public abstract void drawContactPoint(Vector3 PointOnB, Vector3 normalOnB, float distance, int lifeTime, Vector3 color);

	public abstract void reportErrorWarning(String warningString);

	public abstract void draw3dText(Vector3 location, String textString);

	public abstract void setDebugMode(int debugMode);

	public abstract int getDebugMode();

	public void drawAabb(Vector3 from, Vector3 to, Vector3 color) {
	    Stack stack = Stack.enter();
		Vector3 halfExtents = stack.alloc(to);
		halfExtents.sub(from);
		halfExtents.scl(0.5f);

		Vector3 center = stack.alloc(to);
		center.add(from);
		center.scl(0.5f);

		int i, j;

		Vector3 edgecoord = stack.allocVector3();
		edgecoord.set(1f, 1f, 1f);
		Vector3 pa = stack.allocVector3(), pb = stack.allocVector3();
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 3; j++) {
				pa.set(edgecoord.x * halfExtents.x, edgecoord.y * halfExtents.y, edgecoord.z * halfExtents.z);
				pa.add(center);

				int othercoord = j % 3;

				VectorUtil.mulCoord(edgecoord, othercoord, -1f);
				pb.set(edgecoord.x * halfExtents.x, edgecoord.y * halfExtents.y, edgecoord.z * halfExtents.z);
				pb.add(center);

				drawLine(pa, pb, color);
			}
			edgecoord.set(-1f, -1f, -1f);
			if (i < 3) {
				VectorUtil.mulCoord(edgecoord, i, -1f);
			}
		}
		stack.leave();
	}
	
	public void drawBox(Vector3 bbMin, Vector3 bbMax, Transform trans, Vector3 color) {

		Stack stack = Stack.enter();
		
		Vector3 tmp = stack.allocVector3();
		
		Vector3 from = stack.allocVector3();
		Vector3 to = stack.allocVector3();
		
		
		VectorUtil.add(from, trans.origin, tmp.set(bbMin.x, bbMin.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMax.x, bbMin.y, bbMin.z).mul(trans.basis));
		drawLine(from,to, color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMax.x, bbMin.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMax.x, bbMax.y, bbMin.z).mul(trans.basis));
		drawLine(from,to, color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMax.x, bbMax.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMin.x, bbMax.y, bbMin.z).mul(trans.basis));
		drawLine(from, to, color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMin.x, bbMax.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMin.x, bbMin.y, bbMin.z).mul(trans.basis));
		drawLine(from, to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMin.x, bbMin.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMin.x, bbMin.y, bbMax.z).mul(trans.basis));
		drawLine(from , to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMax.x, bbMin.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMax.x, bbMin.y, bbMax.z).mul(trans.basis));
		drawLine(from, to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMax.x, bbMax.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMax.x, bbMax.y, bbMax.z).mul(trans.basis));
		drawLine(from , to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMin.x, bbMax.y, bbMin.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMin.x, bbMax.y, bbMax.z).mul(trans.basis));
		drawLine(from, to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMin.x, bbMin.y, bbMax.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMax.x, bbMin.y, bbMax.z).mul(trans.basis));
		drawLine(from , to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMax.x, bbMin.y, bbMax.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMax.x, bbMax.y, bbMax.z).mul(trans.basis));
		drawLine(from, to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMax.x, bbMax.y, bbMax.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMin.x, bbMax.y, bbMax.z).mul(trans.basis));
		drawLine(from , to , color);
		VectorUtil.add(from, trans.origin, tmp.set(bbMin.x, bbMax.y, bbMax.z).mul(trans.basis));
		VectorUtil.add(to, trans.origin, tmp.set(bbMin.x, bbMin.y, bbMax.z).mul(trans.basis));
		drawLine(from , to , color);
		stack.leave();
		
	}
	
	void drawBox(Vector3 bbMin, Vector3 bbMax, Vector3 color)
	{
		
		Stack stack = Stack.enter();
		Vector3 tmp = stack.allocVector3();
		
		drawLine(tmp.set(bbMin.x, bbMin.y, bbMin.z), tmp.set(bbMax.x, bbMin.y, bbMin.z), color);
		drawLine(tmp.set(bbMax.x, bbMin.y, bbMin.z), tmp.set(bbMax.x, bbMax.y, bbMin.z), color);
		drawLine(tmp.set(bbMax.x, bbMax.y, bbMin.z), tmp.set(bbMin.x, bbMax.y, bbMin.z), color);
		drawLine(tmp.set(bbMin.x, bbMax.y, bbMin.z), tmp.set(bbMin.x, bbMin.y, bbMin.z), color);
		drawLine(tmp.set(bbMin.x, bbMin.y, bbMin.z), tmp.set(bbMin.x, bbMin.y, bbMax.z), color);
		drawLine(tmp.set(bbMax.x, bbMin.y, bbMin.z), tmp.set(bbMax.x, bbMin.y, bbMax.z), color);
		drawLine(tmp.set(bbMax.x, bbMax.y, bbMin.z), tmp.set(bbMax.x, bbMax.y, bbMax.z), color);
		drawLine(tmp.set(bbMin.x, bbMax.y, bbMin.z), tmp.set(bbMin.x, bbMax.y, bbMax.z), color);
		drawLine(tmp.set(bbMin.x, bbMin.y, bbMax.z), tmp.set(bbMax.x, bbMin.y, bbMax.z), color);
		drawLine(tmp.set(bbMax.x, bbMin.y, bbMax.z), tmp.set(bbMax.x, bbMax.y, bbMax.z), color);
		drawLine(tmp.set(bbMax.x, bbMax.y, bbMax.z), tmp.set(bbMin.x, bbMax.y, bbMax.z), color);
		drawLine(tmp.set(bbMin.x, bbMax.y, bbMax.z), tmp.set(bbMin.x, bbMin.y, bbMax.z), color);
		stack.leave();
	}

	public void setDebugMode(DebugModesType debugMode) {
		// TODO Auto-generated method stub
		
	}
	
	
}
