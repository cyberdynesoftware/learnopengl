(ns learnopengl.model-loader
  (:require [learnopengl.error :as error])
  (:import [org.lwjgl.opengl GL33]
           [main.java ModelLoader]
           [org.lwjgl.assimp Assimp AIScene AINode AIMesh AITexture AIMaterial AIString AIVector3D AIVector3D$Buffer AIFace AIFace$Buffer]
           [org.lwjgl.system MemoryUtil]
           [java.nio FloatBuffer IntBuffer]
           [org.lwjgl BufferUtils PointerBuffer]))

(set! *warn-on-reflection* true)

(defn new-mesh
  [indices-count material-index]
  {:vbo (GL33/glGenBuffers)
   :vao (GL33/glGenVertexArrays)
   :ebo (GL33/glGenBuffers)
   :indices-count indices-count
   :material-index material-index})

(comment copy-to-float-buffer
  [vertices number dim]
  (let [buffer (BufferUtils/createFloatBuffer (* number dim))]
    (doseq [vertex (take number (repeatedly #(.get vertices)))]
      (.put buffer (.x vertex))
      (.put buffer (.y vertex))
      (when (= dim 3)
        (.put buffer (.z vertex))))
    (.flip buffer)))

(comment mem-to-float-buffer
  [buffer size]
  (.flip (MemoryUtil/memFloatBuffer (.address0 buffer) size)))

(defn create-vertex-buffer
  [^AIMesh mesh]
  (let [buffer (BufferUtils/createFloatBuffer (* ^int (.mNumVertices mesh) 8))]
    (dotimes [index ^int (.mNumVertices mesh)]
      (let [vertex ^AIVector3D (.get ^AIVector3D$Buffer (.mVertices mesh) index)
            normal ^AIVector3D (.get ^AIVector3D$Buffer (.mNormals mesh) index)
            tex-coords ^AIVector3D (.get ^AIVector3D$Buffer (.mTextureCoords mesh 0) index)]
        (.put buffer ^float (.x vertex))
        (.put buffer ^float (.y vertex))
        (.put buffer ^float (.z vertex))
        (.put buffer ^float (.x normal))
        (.put buffer ^float (.y normal))
        (.put buffer ^float (.z normal))
        (.put buffer ^float (.x tex-coords))
        (.put buffer ^float (.y tex-coords))))
    (.flip buffer)))

(defn load-interleaved-vertex-data
  [aimesh vbo]
  (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
  (let [buffer ^FloatBuffer (create-vertex-buffer aimesh)]
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER buffer GL33/GL_STATIC_DRAW)

    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 32 0)
    (GL33/glEnableVertexAttribArray 0)
    (GL33/glVertexAttribPointer 1 3 GL33/GL_FLOAT false 32 12)
    (GL33/glEnableVertexAttribArray 1)
    (GL33/glVertexAttribPointer 2 2 GL33/GL_FLOAT false 32 24)
    (GL33/glEnableVertexAttribArray 2)))

(comment load-batched-vertex-data
  [aimesh vbo]
  (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
  (let [vertices-size (* (.mNumVertices aimesh) 12)
        tex-coords-size (* (.mNumVertices aimesh) 8)
        vertices (mem-to-float-buffer (.mVertices aimesh) vertices-size)
        normals (mem-to-float-buffer (.mNormals aimesh) vertices-size)
        tex-coords (mem-to-float-buffer (.mTextureCoords aimesh 0) tex-coords-size)]
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER (+ (* vertices-size 2) tex-coords-size) GL33/GL_STATIC_DRAW)

    (GL33/glBufferSubData GL33/GL_ARRAY_BUFFER 0 vertices)
    (GL33/glBufferSubData GL33/GL_ARRAY_BUFFER vertices-size normals)
    (GL33/glBufferSubData GL33/GL_ARRAY_BUFFER (* vertices-size 2) tex-coords)

    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 12 0)
    (GL33/glEnableVertexAttribArray 0)
    (GL33/glVertexAttribPointer 1 3 GL33/GL_FLOAT false 12 vertices-size)
    (GL33/glEnableVertexAttribArray 1)
    (GL33/glVertexAttribPointer 2 2 GL33/GL_FLOAT false 8 (* vertices-size 2))
    (GL33/glEnableVertexAttribArray 2)))

(defn create-index-buffer
  [^AIMesh mesh]
  (let [buffer (BufferUtils/createIntBuffer (* ^int (.mNumFaces mesh) 3))
        faces ^AIFace$Buffer (.mFaces mesh)]
    (doseq [^AIFace face (take ^int (.mNumFaces mesh) (repeatedly #(.get faces)))]
      (assert (= ^int (.mNumIndices face) 3))
      (let [indices ^IntBuffer (.mIndices face)]
        (doseq [^int index (take ^int (.mNumIndices face) (repeatedly #(.get indices)))]
          (.put buffer index))))
    (.flip buffer)))

(defn load-indices
  [^AIMesh aimesh ebo]
  (GL33/glBindBuffer GL33/GL_ELEMENT_ARRAY_BUFFER ebo)
  (let [^IntBuffer buffer (create-index-buffer aimesh)]
    (GL33/glBufferData GL33/GL_ELEMENT_ARRAY_BUFFER buffer GL33/GL_STATIC_DRAW)))

(def texture-types 
  [{:type Assimp/aiTextureType_AMBIENT
    :num-expected 1}
   {:type Assimp/aiTextureType_DIFFUSE
    :num-expected 1}
   {:type Assimp/aiTextureType_SPECULAR
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
                  (format "unexpected texture count: %d for %d"
                          texture-count
                          (:type texture-type)))
          (Assimp/aiGetMaterialString material
                                      Assimp/_AI_MATKEY_TEXTURE_BASE
                                      (:type texture-type)
                                      0
                                      path)
          (.dataString path))))))

(defn process-mesh
  [pointer]
  (let [aimesh (AIMesh/create ^long pointer)
        mesh (new-mesh (* (.mNumFaces aimesh) 3)
                       (.mMaterialIndex aimesh))]
    (GL33/glBindVertexArray (:vao mesh))
    (load-interleaved-vertex-data aimesh (:vbo mesh))
    (load-indices aimesh (:ebo mesh))
    ;(error/check-error)
    mesh))

(defn material-indices
  [meshes]
  (reduce (fn [result mesh]
            (conj result (:material-index mesh)))
          #{}
          meshes))

(defn process-scene
  [^AIScene scene]
  (let [mesh-pointer-buffer ^PointerBuffer (.mMeshes scene)
        meshes (->> (take ^int (.mNumMeshes scene)
                          (repeatedly #(.get mesh-pointer-buffer)))
                    (mapv process-mesh))]
    {:meshes meshes
     :textures (filterv identity (read-texture-name (material-indices meshes) scene))}))

(defn load-model
  "Loads a 3D model from a file."
  [^String path]
  (if-let [scene (Assimp/aiImportFile path (bit-or Assimp/aiProcess_Triangulate
                                                   Assimp/aiProcess_FlipUVs))]
    (time (process-scene scene))
    (throw (ex-info (Assimp/aiGetErrorString) {:path path}))))
