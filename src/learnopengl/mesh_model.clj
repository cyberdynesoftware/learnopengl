(ns learnopengl.mesh-model
  (:import [org.lwjgl.assimp Assimp AINode AIMesh]
           [org.lwjgl BufferUtils]))

(defn create-vertex-buffer
  [mesh]
  (let [buffer (BufferUtils/createFloatBuffer (* (.mNumVertices mesh) 8))
        vertices (.mVertices mesh)
        normals (.mNormals mesh)
        tex-coords (.mTextureCoords mesh 0)]
    (doseq [[vertex normal tex-coords]
            (partition 3 (interleave (take (.mNumVertices mesh) (repeatedly #(.get vertices)))
                                     (take (.mNumVertices mesh) (repeatedly #(.get normals)))
                                     (take (.mNumVertices mesh) (repeatedly #(.get tex-coords)))))]
      (.put buffer (.x vertex))
      (.put buffer (.y vertex))
      (.put buffer (.z vertex))
      (.put buffer (.x normal))
      (.put buffer (.y normal))
      (.put buffer (.z normal))
      (.put buffer (.x tex-coords))
      (.put buffer (.y tex-coords)))
    (.flip buffer)))

(defn read-model
  "Reads a 3D model from a file and returns a vector of AIMesh pointers."
  [^String path]
  (if-let [scene (Assimp/aiImportFile path (bit-or Assimp/aiProcess_Triangulate
                                                   Assimp/aiProcess_FlipUVs))]
    (mapv (fn [^long index]
            (let [mesh (AIMesh/create (.get (.mMeshes scene) index))]
              (create-vertex-buffer mesh)))
          (range (.mNumMeshes scene)))
    (throw (ex-info (Assimp/aiGetErrorString) {:path path}))))
