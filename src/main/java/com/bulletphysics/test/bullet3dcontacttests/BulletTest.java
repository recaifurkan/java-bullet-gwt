
package com.bulletphysics.test.bullet3dcontacttests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.math.Vector3;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.ContactAddedCallback;
import com.bulletphysics.LibgdxDebugDrawer;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;

import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.test.bullet3dcontacttests.BulletTest.GameObject.Constructor;





public class BulletTest implements ApplicationListener {
	final static short GROUND_FLAG = 1 << 8;
	final static short OBJECT_FLAG = 1 << 9;
	final static short ALL_FLAG = -1;

	
	private LibgdxDebugDrawer debugDrawer;
	
	class MyContactListener extends ContactAddedCallback {
		

		@Override
		public boolean contactAdded(ManifoldPoint cp, CollisionObject colObj0, int partId0, int index0,
				CollisionObject colObj1, int partId1, int index1) {
			
			Object col0 = colObj0.getUserPointer();
			Object col1 = colObj1.getUserPointer();
			
			if(col0 !=null) {
				((ColorAttribute)getInstances().get((Integer) col0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
			}
			
			if(col1 !=null) {
				((ColorAttribute)getInstances().get((Integer) col1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);	
			}
			
				
			
			return true;
		}
	}

	static class MyMotionState extends MotionState {
		Transform transform;

		

		@Override
		public Transform getWorldTransform(Transform out) {
			transform.set(out);
			return null;
		}

		@Override
		public void setWorldTransform(Transform worldTrans) {
			worldTrans.set(transform);
			
		}
	}

	static class GameObject extends ModelInstance implements Disposable {
		public final RigidBody body;
		public final MyMotionState motionState;

		public GameObject (Model model, String node, RigidBodyConstructionInfo constructionInfo) {
			super(model, node);
			motionState = new MyMotionState();
			motionState.transform = new Transform(transform);
			body = new RigidBody(constructionInfo);
			body.setMotionState(motionState);
		}

		@Override
		public void dispose () {
		}

		static class Constructor implements Disposable {
			public final Model model;
			public final String node;
			public final CollisionShape shape;
			public final RigidBodyConstructionInfo constructionInfo;
			private static Vector3 localInertia = new Vector3();

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
			public void dispose () {
			}
		}
	}

	PerspectiveCamera cam;
	FirstPersonCameraController camController;
	ModelBatch modelBatch;
	Environment environment;
	Model model;
	public static Array<GameObject> instances;
	ArrayMap<String, Object> constructors;
	float spawnTimer;

	CollisionConfiguration collisionConfig;
	Dispatcher dispatcher;
	MyContactListener contactListener;
	BroadphaseInterface broadphase;
	DynamicsWorld dynamicsWorld;
	ConstraintSolver constraintSolver;

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

		
		
		Input input = new Input(this);
		camController = new FirstPersonCameraController(cam);
		InputMultiplexer multiplexer = new InputMultiplexer(input,camController);
		Gdx.input.setInputProcessor(multiplexer);

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
		constraintSolver = new SequentialImpulseConstraintSolver();
		setDynamicsWorld(new DiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig));
		getDynamicsWorld().setGravity(new Vector3(0, -10f, 0));
		setDebugDrawer(new LibgdxDebugDrawer());
		dynamicsWorld.setDebugDrawer(getDebugDrawer());
		

		setInstances(new Array<GameObject>());
		GameObject object = ((Constructor) constructors.get("ground")).construct();
//	
		getInstances().add(object);
		object.body.setUserPointer(getInstances().size);
		
		object.body.setCollisionFlags(object.body .getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);  
		object.body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		getDynamicsWorld().addRigidBody(object.body);
//		object.body.setCollisionFlags(GROUND_FLAG);
//		
//		object.body.setContactCallbackFilter(0);
//		
//		object.body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

		
	}

	public synchronized void spawn () {
		GameObject obj = ((Constructor) constructors.values[1 + MathUtils.random(constructors.size - 2)]).construct();
		obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
		obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
		obj.body.proceedToTransform(new Transform(obj.transform));
		obj.body.setUserPointer(getInstances().size);
		obj.body.setCollisionFlags(obj.body.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
		getInstances().add(obj);
//		obj.body.setCollisionFlags(OBJECT_FLAG);
		getDynamicsWorld().addRigidBody(obj.body);
		
//		obj.body.setContactCallbackFilter(GROUND_FLAG);
	}

	float angle, speed = 90f;

	@Override
	public void render () {
		
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

		angle = (angle + delta * speed) % 360f;
//		getInstances().get(0).body.translate(new Vector3(0, MathUtils.sinDeg(angle) * 0.15f, 0f));
		
		
		for (int i = 0; i < getInstances().size ; i++) {
			GameObject obj = getInstances().get(i);
			Transform tmp = new Transform();
			obj.body.getWorldTransform(tmp);
			tmp.getMatrix(obj.transform);
			
			
		}

		getDynamicsWorld().stepSimulation(delta, 5, 1f / Gdx.graphics.getFramesPerSecond());

		if ((spawnTimer -= delta) < 0) {
			spawn();
			spawnTimer = 0.25f;
		}

		camController.update();

		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		


		modelBatch.begin(cam);
		modelBatch.render(getInstances(), environment);
		modelBatch.end();

		
		getDebugDrawer().begin(cam);
		dynamicsWorld.debugDrawWorld();
		getDebugDrawer().end();
		
		
	}

	@Override
	public void dispose () {
		for (GameObject obj : getInstances())
			obj.dispose();
		getInstances().clear();

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

	/**
	 * @return the instances
	 */
	public Array<GameObject> getInstances() {
		return instances;
	}

	/**
	 * @param instances the instances to set
	 */
	public void setInstances(Array<GameObject> instances) {
		this.instances = instances;
	}

	/**
	 * @return the dynamicsWorld
	 */
	public DynamicsWorld getDynamicsWorld() {
		return dynamicsWorld;
	}

	/**
	 * @param dynamicsWorld the dynamicsWorld to set
	 */
	public void setDynamicsWorld(DynamicsWorld dynamicsWorld) {
		this.dynamicsWorld = dynamicsWorld;
	}

	/**
	 * @return the debugDrawer
	 */
	public LibgdxDebugDrawer getDebugDrawer() {
		return debugDrawer;
	}

	/**
	 * @param debugDrawer the debugDrawer to set
	 */
	public void setDebugDrawer(LibgdxDebugDrawer debugDrawer) {
		this.debugDrawer = debugDrawer;
	}


}
