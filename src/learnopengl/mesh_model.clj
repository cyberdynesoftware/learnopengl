(ns learnopengl.mesh-model
  (:import [org.lwjgl.assimp Assimp AINode AIMesh AIMaterial]
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

(defn create-index-buffer
  [mesh]
  (let [buffer (BufferUtils/createIntBuffer (* (.mNumFaces mesh) 3))
        faces (.mFaces mesh)]
    (doseq [face (take (.mNumFaces mesh) (repeatedly #(.get faces)))]
      (assert (= (.mNumIndices face) 3))
      (let [indices (.mIndices face)]
        (doseq [index (take (.mNumIndices face) (repeatedly #(.get indices)))]
          (.put buffer index))))))

(defn read-textures
  [mesh scene]
  (assert (= (.mMaterialIndex mesh) 1))
  (let [material-pointer (.get (.mMaterials scene) (.mMaterialIndex mesh))]
    (println material-pointer)
  ))

(defn read-model-new
  "Reads a 3D model from a file and returns a vector of AIMesh pointers."
  [^String path]
  (if-let [scene (Assimp/aiImportFile path (bit-or Assimp/aiProcess_Triangulate
                                                   Assimp/aiProcess_FlipUVs))]
    (mapv (fn [^long index]
            (let [mesh (AIMesh/create (.get (.mMeshes scene) index))]
              (println "reading mesh")
              ;(create-vertex-buffer mesh)
              ;(create-index-buffer mesh)
              (read-textures mesh scene)))
          (range (.mNumMeshes scene)))
    (throw (ex-info (Assimp/aiGetErrorString) {:path path}))))

(defn read-model
  "read a 3D model from a file"
  [path]
  (let [scene (Assimp/aiImportFile path (bit-or Assimp/aiProcess_Triangulate
                                                Assimp/aiProcess_FlipUVs))]
    (if (= scene nil)
      (println (Assimp/aiGetErrorString))
      (for [index (range (.mNumMeshes scene))]
        (let [pointer (.get (.mMeshes scene) index)]
          (assert (= (type pointer) java.lang.Long))
          (AIMesh/create pointer))))))
