
package com.byrfb.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.bulletphysics.LibgdxDebugDrawer;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DebugModesType;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.byrfb.tests.objects.Constructor;
import com.byrfb.tests.objects.GameObject;

/**
 * Eğer fizik kütüphanesinde bir değişiklik yaparsan direk build artifact de düzelir
 * <p>
 * <p>
 * Bu sınıftaki kod denemenle rigid body fiziğin iyice öğrenmeyi yaptın
 * triger olarak kullanmka istiyorsan ghost object oluşturryrsyn
 * bu ghost objecte no contact response dersin ve
 * fiziksel olrak hesaplanması yapılmaz
 * sadece collisison olduğu zaman collision added fonksiyonunn çağrılasını sağlar
 * ghost için ayrı bir yöntem daha var istersen onu da kullan ama her defasında oyun
 * dönügünse onu çağırman gerekyor
 * şu an da tam da istediğin gbi çalışma işlemini yapıyor
 * yani son olarak trgiger olarak ghost object kullanırsın
 * ya da başka bir şekilde kullanabilirsin galiba ama dur bakalım rigi body de olacak mı?
 * Evet rigid body de de denendi
 * onda da no contact respnse dersen sadece triger olarak kullanbilrisin
 * bu şekilde nerdeyse kafanda olan bütün fizik sistemlerinin oluşturulmasını öğrenmiş olduun
 * BİR TANE FİZİK OBJESİ OLUŞTURMK İÇİN
 * --------------------
 * 1-önce colisionshape yapıalcak
 * 2-sonra coolilision object yapılacak
 * 3-collision objecte collision shape eklenecek
 * 4-sonra son olarak dünyaya da collision objecti ekleyeceksin
 * Rigid body için ise
 * shape belirlenecek
 * rigidbodyconstructioninfo belirlenecek
 * bu info da mass,motionstate,collisionshape istiyor sonra rigidbodyye bunu verecen sonra dünyaya ekleyeceksin
 */
public class BulletTestContackCallbak implements ApplicationListener {
    PerspectiveCamera cam;
    CameraInputController camController;
    ModelBatch modelBatch;
    Environment environment;
    Model model;
    Array<GameObject> instances = new Array<GameObject>();
    ArrayMap<String, Object> constructors;
    float spawnTimer;

    CollisionConfiguration collisionConfig;
    Dispatcher dispatcher;
    MyContactListener contactListener;
    BroadphaseInterface broadphase;
    DiscreteDynamicsWorld collisionWorld;
    LibgdxDebugDrawer debugDrawer;
    GhostObject ghost;


    @Override
    public void create() {
        modelBatch = new ModelBatch();

        setUpEnvironment();

        setUpCamera();

        Gdx.input.setInputProcessor(camController);

        setUpObjects();

        setUpPhysic();


//        setUpGhost();
//		setUpCompoundShapeExample();
        setUpGround();
        spawn();
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        inputLoop();
//		contactListen();

        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
        collisionWorld.stepSimulation(1 / 60f);

        for (GameObject obj : instances) {
            if (obj.moving) {

                Transform tr = new Transform();
                obj.body.getWorldTransform(tr);
                tr.getMatrix(obj.transform);

            }
        }




//        spawnLoop(delta);






        camController.update();
        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();


        debugDrawer.begin(cam);
        collisionWorld.debugDrawWorld();
        debugDrawer.end();

    }

    private void inputLoop() {
        /**
         * Eğer ctrl ile birlikte tıklarsan spawnlanan onjeye kuvvet uygularsın
         */
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                Vector3 force = new Vector3(cam.direction);
                force.scl(50);
                obj.body.applyCentralForce(force);
            }
        }
    }

    private void spawnLoop(float delta) {
        if ((spawnTimer -= delta) < 0) {
            spawn();
            spawnTimer = 1f;
        }
    }


    private void setUpEnvironment() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    private void setUpCamera() {
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(3f, 7f, 10f);
        cam.lookAt(0, 4f, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        camController = new CameraInputController(cam);
    }

    private void setUpObjects() {
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
    }

    private void setUpPhysic() {
        contactListener = new MyContactListener();
        BulletGlobals.setContactAddedCallback(contactListener);
        collisionConfig = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(collisionConfig);
        broadphase = new DbvtBroadphase();

        SequentialImpulseConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();

        collisionWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig) {
        };
        collisionWorld.setGravity(new Vector3(0, -1.8f, 0));

				/*
		Bu alttaki ile ghost objecte collision avr mı çok rahat bir şekilde anlayabilirsin
		 */
//		collisionWorld.getPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        setUpDebugDrawer();
    }

    private void setUpDebugDrawer() {
		/*
		Debug drawer kendi yaptığın eksiklerini tamamladığın bişey normalde wireframe ile bişey göremezsin ama onu ayarladık

		 */
        debugDrawer = new LibgdxDebugDrawer();
        collisionWorld.setDebugDrawer(debugDrawer);
        debugDrawer.setDebugMode(DebugModesType.MAX_DEBUG_DRAW_MODE);
    }

    private void setUpGhost() {
		/*
		Ghost object diğer rigit bodyler ile no contack yaparsan triger olarak çok rahat bir şekilde kullanabilirsin
		hem de bu seferdünyaya acayip şeler yapman gerekmez
		normal contack listener ile sadece triger olarak kullanabilrisin

		 */
        CollisionShape shape = new BoxShape(new Vector3(2.5f, 2.5f, 2.5f));
        ghost = new GhostObject();
        ghost.setCollisionShape(shape);
        Transform tf = new Transform();
        ghost.getWorldTransform(tf);
        tf.origin.set(0, 0, 0);
        ghost.setWorldTransform(tf);
        ghost.setCollisionFlags(ghost.getCollisionFlags() | CollisionFlags.NO_CONTACT_RESPONSE);
        collisionWorld.addCollisionObject(ghost);
    }

    private void setUpGround() {
        GameObject object = ((Constructor) constructors.get("ground")).construct();
        object.body.setCollisionFlags(object.body.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
        instances.add(object);
        collisionWorld.addRigidBody(object.body);
    }

    private void setUpCompoundShapeExample() {
        CompoundShape mainShape = new CompoundShape();
        BoxShape shape1 = new BoxShape(new Vector3(1, 1, 1));
        BoxShape shape2 = new BoxShape(new Vector3(1, 1, 1));
        SphereShape sphere = new SphereShape(5);
        CapsuleShape cone = new CapsuleShape(2, 5);
        Transform tr = new Transform();
        tr.origin.set(0, 0, 1);
        mainShape.addChildShape(tr, shape1);
        tr.origin.set(0, 0, -1);
        mainShape.addChildShape(tr, cone);
        tr.origin.set(0, 1, 0);
        mainShape.addChildShape(tr, shape2);
        tr.origin.set(0, -1, 0);
        mainShape.addChildShape(tr, sphere);
        RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(0,
                new MotionState() {
                    @Override
                    public Transform getWorldTransform(Transform out) {
                        return null;
                    }

                    @Override
                    public void setWorldTransform(Transform worldTrans) {

                    }
                }, mainShape);

        RigidBody body = new RigidBody(info);
        body.setActivationState(-1);
        body.setCollisionFlags(body.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
        collisionWorld.addRigidBody(body);

    }


    /*
    objeye erişebilme için sınıf değişkeni yaptık

     */
    GameObject obj;

    public void spawn() {
        obj = ((Constructor) constructors.values[1 + MathUtils.random(constructors.size - 2)]).construct();
        obj.moving = true;
        obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));

        obj.body.setWorldTransform(new Transform(obj.transform));
        obj.body.setUserPointer(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
        instances.add(obj);
        collisionWorld.addRigidBody(obj.body);

    }


    /**
     * Bu alttaki ile Çok güze lbir şekilde triger işlemi yaptırabilirsin
     * ghost objectin trigerleme işemine bakılıyor
     */
    void contactListen() {


        ObjectArrayList<CollisionObject> pairs = ghost.getOverlappingPairs();


        int size = pairs.size();
        if (size > 0) {
            System.out.println("Collision From PairListen");
        }
        for (int i = 0; i < size; i++) {
            CollisionObject obj = pairs.get(i);

            Object col0 = obj.getUserPointer();

            if (col0 != null) {
//				instances.get((Integer) col0).moving = false;
            }
        }

        pairs.clear();


    }


    @Override
    public void dispose() {
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
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
    }

    class MyContactListener extends ContactAddedCallback {


        @Override
        public boolean contactAdded(ManifoldPoint cp, CollisionObject colObj0, int partId0, int index0,
                                    CollisionObject colObj1, int partId1, int index1) {


            if (colObj0 == null || colObj1 == null) return true;
            System.out.println("Collision from Conract Listener");

            Object col0 = colObj0.getUserPointer();
            Object col1 = colObj1.getUserPointer();


//            if (col0 != null) {
//                instances.get((Integer) col0).moving = false;
//            }
//
//            if (col1 != null) {
//                instances.get((Integer) col1).moving = false;
//            }


            return true;
        }
    }
}
