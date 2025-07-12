(ns learnopengl.mesh-model
  (:import [org.lwjgl.assimp Assimp AINode AIMesh AITexture AIMaterial AIString]
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

(def model
  {:vertices []
   :indices []
   :material-indices #{}
   :textures []})

(def texture-types 
  [{:pkey Assimp/AI_MATKEY_COLOR_AMBIENT
    :type Assimp/aiTextureType_AMBIENT
    :num-expected 1}
   {:pkey Assimp/AI_MATKEY_NAME
    :type Assimp/aiTextureType_DIFFUSE
    :num-expected 1}
   {:pkey Assimp/AI_MATKEY_COLOR_SPECULAR
    :type Assimp/aiTextureType_SPECULAR
    :num-expected 1}])

(defn read-texture-name
  [material-indices scene]
  (assert (= (count material-indices) 1)
          "single material index expected")
  (let [material-pointer (.get (.mMaterials scene) (first material-indices))
        material (AIMaterial/create ^long material-pointer)]
    (for [texture-type texture-types]
      (let [texture-count (Assimp/aiGetMaterialTextureCount material (:type texture-type))
            path (AIString/create)]
        (when (> texture-count 0)
          (assert (= texture-count (:num-expected texture-type))
                  (format "unexpected texture count: %d for %s"
                          texture-count
                          (:pkey texture-type)))
          (Assimp/aiGetMaterialTexture material
                                       (:type texture-type)
                                       0
                                       path
                                       nil nil nil nil nil nil)
          (.dataString path))))))

(defn read-scene
  [scene]
  (let [mesh-pointer-buffer (.mMeshes scene)
        result (reduce (fn [model mesh-pointer]
                         (let [mesh (AIMesh/create ^long mesh-pointer)]
                           (-> model
                               (update-in [:vertices]
                                          #(conj % (create-vertex-buffer mesh)))
                               (update-in [:indices]
                                          #(conj % (create-index-buffer mesh)))
                               (update-in [:material-indices]
                                          #(conj % (.mMaterialIndex mesh))))))
                       model
                       (take (.mNumMeshes scene)
                             (repeatedly #(.get mesh-pointer-buffer))))]
    (assoc-in result
              [:textures]
              (filterv identity (read-texture-name (:material-indices result) scene)))))

(defn read-model
  "Reads a 3D model from a file and returns a vector of AIMesh pointers."
  [^String path]
  (if-let [scene (Assimp/aiImportFile path (bit-or Assimp/aiProcess_Triangulate
                                                   Assimp/aiProcess_FlipUVs))]
    (read-scene scene)
    (throw (ex-info (Assimp/aiGetErrorString) {:path path}))))
