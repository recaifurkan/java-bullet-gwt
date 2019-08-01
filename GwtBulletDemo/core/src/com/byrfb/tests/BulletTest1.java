
package com.byrfb.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.ContactAddedCallback;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.Transform;
import com.byrfb.tests.objects.Constructor;
import com.byrfb.tests.objects.GameObject;

/** @see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
 * @author Xoppa */
public class BulletTest1 implements ApplicationListener {
	final static short GROUND_FLAG = 1 << 8;
	final static short OBJECT_FLAG = 1 << 9;
	final static short ALL_FLAG = -1;
	


	class MyContactListener extends ContactAddedCallback {
		

		@Override
		public boolean contactAdded(ManifoldPoint cp, CollisionObject colObj0, int partId0, int index0,
				CollisionObject colObj1, int partId1, int index1) {
			
			Object col0 = colObj0.getUserPointer();
			Object col1 = colObj1.getUserPointer();
			System.out.println("Collision");
			
			
			if(col0 !=null) {
				instances.get((Integer) col0).moving = false;
			}
			
			if(col1 !=null) {
				instances.get((Integer) col1).moving = false;	
			}
			
				
			
			return true;
		}
	}



	PerspectiveCamera cam;
	CameraInputController camController;
	ModelBatch modelBatch;
	Environment environment;
	Model model;
	Array<GameObject> instances;
	ArrayMap<String, Object> constructors;
	float spawnTimer;

	CollisionConfiguration collisionConfig;
	Dispatcher dispatcher;
	MyContactListener contactListener;
	BroadphaseInterface broadphase;
	CollisionWorld collisionWorld;

	@Override
	public void create () {
		

		contactListener = new MyContactListener();
		BulletGlobals.setContactAddedCallback(contactListener);
		
		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(3f, 7f, 10f);
		cam.lookAt(0, 4f, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		mb.node().id = "ground";
		mb.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
			.box(5f, 1f, 5f);
		mb.node().id = "sphere";
		mb.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
			.sphere(1f, 1f, 1f, 10, 10);
		mb.node().id = "box";
		mb.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
			.box(1f, 1f, 1f);
		mb.node().id = "cone";
		mb.part("cone", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
			.cone(1f, 2f, 1f, 10);
		mb.node().id = "capsule";
		mb.part("capsule", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.CYAN)))
			.capsule(0.5f, 2f, 10);
		mb.node().id = "cylinder";
		mb.part("cylinder", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
			new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 2f, 1f, 10);
		model = mb.end();

		
		constructors = new ArrayMap<String, Object>(String.class, Object.class);
		constructors.put("ground", new Constructor(model, "ground", new BoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
		constructors.put("sphere", new Constructor(model, "sphere", new SphereShape(0.5f), 1f));
		constructors.put("box", new Constructor(model, "box", new BoxShape(new Vector3(0.5f, 0.5f, 0.5f)), 1f));
		constructors.put("cone", new Constructor(model, "cone", new ConeShape(0.5f, 2f), 1f));
		constructors.put("capsule", new Constructor(model, "capsule", new CapsuleShape(.5f, 1f), 1f));
		constructors.put("cylinder", new Constructor(model, "cylinder", new CylinderShape(new Vector3(.5f, 1f, .5f)),
			1f));

		collisionConfig = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfig);
		broadphase = new DbvtBroadphase();
		collisionWorld = new CollisionWorld(dispatcher, broadphase, collisionConfig);
		

		instances = new Array<GameObject>();
		GameObject object = ((Constructor) constructors.get("ground")).construct();
		instances.add(object);
		collisionWorld.addCollisionObject(object.body, GROUND_FLAG, ALL_FLAG);
	}

	public void spawn () {
		GameObject obj = ((Constructor) constructors.values[1 + MathUtils.random(constructors.size - 2)]).construct();
		obj.moving = true;
		obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
		obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
		obj.body.setWorldTransform(new Transform(obj.transform));
		obj.body.setUserPointer(instances.size);
		obj.body.setCollisionFlags(obj.body.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
		instances.add(obj);
		collisionWorld.addCollisionObject(obj.body, OBJECT_FLAG, GROUND_FLAG);
	}

	@Override
	public void render () {
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

		for (GameObject obj : instances) {
			if (obj.moving) {
				obj.transform.trn(0f, -delta, 0f);
				
				obj.body.setWorldTransform(new Transform(obj.transform));
			}
		}

		collisionWorld.performDiscreteCollisionDetection();

		if ((spawnTimer -= delta) < 0) {
			spawn();
			spawnTimer = 1.5f;
		}

		camController.update();

		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	@Override
	public void dispose () {
		for (GameObject obj : instances)
			obj.dispose();
		instances.clear();

		for (Object ctor : constructors.values())
			((Constructor) ctor).dispose();
		constructors.clear();



		modelBatch.dispose();
		model.dispose();
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
	}
}
