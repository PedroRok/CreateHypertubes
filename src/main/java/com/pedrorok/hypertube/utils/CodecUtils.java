package com.pedrorok.hypertube.utils;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/04/2025
 * @project Create Hypertube
 */
public class CodecUtils {

    /*public static StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
        @Override
        public @NotNull Vec3 decode(ByteBuf buffer) {
            return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public void encode(ByteBuf buffer, Vec3 value) {
            buffer.writeDouble(value.x);
            buffer.writeDouble(value.y);
            buffer.writeDouble(value.z);
        }

    };

    public static StreamCodec<ByteBuf, Integer> INTEGER = new StreamCodec<>() {
        @Override
        public @NotNull Integer decode(ByteBuf byteBuf) {
            return byteBuf.readInt();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeInt(integer);
        }
    };
    public static StreamCodec<ByteBuf, List<Vec3>> VEC3_LIST = new StreamCodec<>() {
        @Override
        public List<Vec3> decode(ByteBuf byteBuf) {
            int size = byteBuf.readInt();
            List<Vec3> vec3s = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                vec3s.add(VEC3.decode(byteBuf));
            }
            return vec3s;
        }

        @Override
        public void encode(ByteBuf o, List<Vec3> vec3s) {
            o.writeInt(vec3s.size());
            for (Vec3 vec3 : vec3s) {
                VEC3.encode(o, vec3);
            }
        }


    };*/
}
