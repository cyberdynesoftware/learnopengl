(ns learnopengl.model-scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.mesh-model :as model]
            [learnopengl.camera :as camera]
            [learnopengl.opengl-helper :as ogl]
            [learnopengl.lights :as lights])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(def model-matrix (doto (new Matrix4f)
                    (.translate (new Vector3f (float 0)))
                    (.scale (new Vector3f (float 1)))))

(def path "resources/assets/backpack/")
(def backpack "backpack.obj")

(defn load-model
  [asset]
  (mapv (fn [[vertex-buffer index-buffer]]
          {:vao (ogl/load-vertices-and-indices vertex-buffer index-buffer)
           :index-count (.capacity index-buffer)})
        (partition 2 (interleave (:vertices asset) (:indices asset)))))

(defn create
  []
  (let [asset (model/read-model (format "%s%s" path backpack))
        shader (shader/get-shader-program
                 (slurp "resources/shaders/model.vert")
                 (slurp "resources/shaders/model.frag"))
        diffuse-texture (ogl/load-texture (format "%s%s" path (first (:textures asset))))
        specular-texture (ogl/load-texture (format "%s%s" path (second (:textures asset))))
        vaos (load-model asset)]
    (GL33/glUseProgram shader)

    (shader/load-int shader "material.diffuse" 0)
    (shader/load-int shader "material.specular" 1)
    (shader/load-float1 shader "material.shininess" 32)

    (lights/init-directional-light shader)
    (lights/init-spotlight shader)

    {:asset asset
     :shader shader
     :diffuse-texture diffuse-texture
     :specular-texture specular-texture
     :vaos vaos}))

(defn render
  [scene delta]
  (let [shader (:shader scene)]
    (GL33/glUseProgram shader)

    (shader/load-matrix shader "projection" (camera/perspective))
    (shader/load-matrix shader "view" (camera/view))
    (shader/load-vector3 shader "viewPos" camera/position)

    (shader/load-matrix shader "model" model-matrix)

    (shader/load-vector3 shader "spotLight.position" camera/position)
    (shader/load-vector3 shader "spotLight.direction" camera/front)

    (GL33/glActiveTexture GL33/GL_TEXTURE0)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:diffuse-texture scene))

    (GL33/glActiveTexture GL33/GL_TEXTURE1)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:specular-texture scene))

    (doseq [vao (:vaos scene)]
      (GL33/glBindVertexArray (:vao vao))
      (GL33/glDrawElements GL33/GL_TRIANGLES (:index-count vao) GL33/GL_UNSIGNED_INT 0))))
