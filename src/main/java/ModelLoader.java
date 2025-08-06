package main.java;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.AIMesh;

public class ModelLoader {
    public static FloatBuffer createInterleavedVertexBuffer(AIMesh mesh) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(mesh.mNumVertices() * 8);
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            var vertex = mesh.mVertices().get(i);
            buffer.put(vertex.x());
            buffer.put(vertex.y());
            buffer.put(vertex.z());

            var normal = mesh.mNormals().get(i);
            buffer.put(normal.x());
            buffer.put(normal.y());
            buffer.put(normal.z());

            var texCoords = mesh.mTextureCoords(0).get(i);
            buffer.put(texCoords.x());
            buffer.put(texCoords.y());
        }
        return buffer.flip();
    }
}
