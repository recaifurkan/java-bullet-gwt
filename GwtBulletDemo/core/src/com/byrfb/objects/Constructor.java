package com.byrfb.objects;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.test.bullet3dcontacttests.objects.GameObject;

public class Constructor implements Disposable {
	public final Model model;
	public final String node;
	public final CollisionShape shape;
	public final RigidBodyConstructionInfo constructionInfo;
	private final Vector3 localInertia = new Vector3();

	public Constructor (Model model, String node, CollisionShape shape, float mass) {
		this.model = model;
		this.node = node;
		this.shape = shape;
		if (mass > 0f)
			shape.calculateLocalInertia(mass, localInertia);
		else
			localInertia.set(0, 0, 0);
		this.constructionInfo = new RigidBodyConstructionInfo(mass, null, shape, localInertia);
	}

	public GameObject construct () {
		return new GameObject(model, node, constructionInfo);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	
}