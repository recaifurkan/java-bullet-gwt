package com.byrfb.tests.objects;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;


public class GameObject extends ModelInstance implements Disposable {
		public final RigidBody body;
		public boolean moving;

		public GameObject (Model model, String node, RigidBodyConstructionInfo constructionInfo) {
			super(model, node);
			body = new RigidBody(constructionInfo);
		}

		

		



		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
	}