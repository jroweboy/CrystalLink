package com.mygdx.game.net;

import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.Bag;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.SnapshotArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;

public class NetworkCommon {


    public static int DEFAULT_TCP_PORT = 59919;
    public static int DEFAULT_UDP_PORT = 59920;

    // both client and server need to have these registered
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.setDefaultSerializer(TaggedFieldSerializer.class);
        kryo.register(NetworkEntity.class);
        kryo.register(NetworkTransformComponent.class);
//        kryo.register(NetworkStateComponent.class);

        // various classes that I were referenceing without realizing it?
//        kryo.register(Signal.class);
//        kryo.register(SnapshotArray.class);
//        kryo.register(Object[].class);
//        kryo.register(Bits.class);
//        kryo.register(long[].class);
//        kryo.register(Bag.class);
//        kryo.register(Array.class);
//        kryo.register(ImmutableArray.class);
    }

    public static class GameConnection extends Connection {
        public NetworkEntity player;
    }
}
