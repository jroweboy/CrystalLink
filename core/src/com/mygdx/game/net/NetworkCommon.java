package com.mygdx.game.net;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.mygdx.game.Assets;
import com.mygdx.game.component.*;
import com.mygdx.game.component.basecomponent.State;
import com.mygdx.game.component.basecomponent.Transform;
import com.mygdx.game.system.RenderingSystem;

public class NetworkCommon {


    public static int DEFAULT_TCP_PORT = 59919;
    public static int DEFAULT_UDP_PORT = 59920;
//    private static TaggedFieldSerializer serializer = new TaggedFieldSerializer();
    // both client and server need to have these registered
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        // regular classes
        kryo.register(NetworkEntity.class);
        kryo.register(NetworkNewPlayer.class);

        // tagged classes
//        TaggedFieldSerializer ser1 = new TaggedFieldSerializer(kryo, NetworkTransformComponent.class);
//        kryo.register(NetworkTransformComponent.class, ser1);
//        TaggedFieldSerializer ser2 = new TaggedFieldSerializer(kryo, Transform.class);

        kryo.register(Transform.class);
        kryo.register(State.class);

        // other classes
        kryo.register(Vector3.class);
        kryo.register(Vector2.class);
        kryo.register(Object[].class);


        // various classes that I were referenceing without realizing it?
//        kryo.register(Signal.class);
//        kryo.register(SnapshotArray.class);
//        kryo.register(Bits.class);
//        kryo.register(long[].class);
//        kryo.register(Bag.class);
//        kryo.register(Array.class);
//        kryo.register(ImmutableArray.class);
    }

    public static class GameConnection extends Connection {
        public Entity player;
    }

    public static void setupNetworkPlayer(Entity entity) {
        //Entity entity = new Entity();

        AnimationComponent animation = new AnimationComponent();
//        PlayerComponent player = new PlayerComponent();
        MovementComponent movement = new MovementComponent();
        TransformComponent transform = new TransformComponent();
        StateComponent state = new StateComponent();

        animation.animations.put(State.NORTH, Assets.get().animations.get("playerWalkNorth"));
        animation.animations.put(State.SOUTH, Assets.get().animations.get("playerWalkSouth"));
        animation.animations.put(State.EAST, Assets.get().animations.get("playerWalkEast"));
        animation.animations.put(State.WEST, Assets.get().animations.get("playerWalkWest"));

        animation.animation_normals.put(State.NORTH, Assets.get().animations.get("playerWalkNorthNormal"));
        animation.animation_normals.put(State.SOUTH, Assets.get().animations.get("playerWalkSouthNormal"));
        animation.animation_normals.put(State.EAST, Assets.get().animations.get("playerWalkEastNormal"));
        animation.animation_normals.put(State.WEST, Assets.get().animations.get("playerWalkWestNormal"));

//        bounds.bounds.width = transform.width;
//        bounds.bounds.height = transform.height;
        TextureComponent texture = new TextureComponent(
                animation.animations.get(State.SOUTH).getKeyFrame(0),
                animation.animation_normals.get(State.SOUTH).getKeyFrame(0));
        TiledMap map = Assets.get().currentMap;
        MapProperties spawn_point = map.getLayers().get("Spawn").getObjects().get(0).getProperties();
        float x = spawn_point.get("x", Float.class) * RenderingSystem.unitScale;
        float y = spawn_point.get("y", Float.class) * RenderingSystem.unitScale;
//        CollisionComponent bounds = new CollisionComponent(x, y, 30, 24);
        transform.c.pos.set(x, y, 0);

        state.c.set(State.STATE_IDLE);

        entity.add(animation);
//        entity.add(player);
//        entity.add(bounds);
        entity.add(movement);
        entity.add(transform);
        entity.add(state);
        entity.add(texture);
    }
}
