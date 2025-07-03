(ns learnopengl.mesh-model
  (:import [org.lwjgl.assimp Assimp AINode AIMesh]))

(defn process-mesh
  [mesh scene]
  (println "foo")
  )

(defn create-child-nodes
  [children-pointer-buffer]
  (loop [result []]
    (if (.hasRemaining children-pointer-buffer)
      (recur (conj result (AINode/create (.get children-pointer-buffer))))
      result)))

(defn process-node
  [node scene]
  (when (> (.mNumMeshes node) 0)
    (println (format "mesh: %d" (.get (.mMeshes node)))))
  (when (> (.mNumChildren node) 0)
    (doseq [child-node (create-child-nodes (.mChildren node))]
      (process-node child-node scene))))

(defn read-model
  "read a 3D model from a file"
  [path]
  (let [scene (Assimp/aiImportFile path (bit-or Assimp/aiProcess_Triangulate
                                                Assimp/aiProcess_FlipUVs))]
    (if (= scene nil)
      (println (Assimp/aiGetErrorString))
      (for [index (range (.mNumMeshes scene))]
        (do
          (println index)
          (AIMesh/create (.get (.mMeshes scene) index)))))))

       (comment (( 
      (let [meshes (.mMeshes scene)]
        (->> (take (.mNumMeshes scene) (repeatedly #(.get meshes)))
             (map #(AIMesh/create %))
             (map #(process-mesh % scene))
             )))))
