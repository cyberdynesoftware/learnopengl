(ns learnopengl.model-scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.model-loader :as model]
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

(defn create
  []
  (let [backpack-model (model/load-model (format "%s%s" path backpack))
        shader (shader/get-shader-program
                 (slurp "resources/shaders/model.vert")
                 (slurp "resources/shaders/model.frag"))
        diffuse-texture (ogl/load-texture (format "%s%s" path (first (:textures backpack-model))))
        specular-texture (ogl/load-texture (format "%s%s" path (second (:textures backpack-model))))]
    (GL33/glUseProgram shader)

    (shader/load-int shader "material.diffuse" 0)
    (shader/load-int shader "material.specular" 1)
    (shader/load-float1 shader "material.shininess" 32)

    (lights/init-directional-light shader)
    (lights/init-spotlight shader)

    {:asset backpack-model
     :shader shader
     :diffuse-texture diffuse-texture
     :specular-texture specular-texture}))

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

    (doseq [mesh (get-in scene [:asset :meshes])]
      (GL33/glBindVertexArray (:vao mesh))
      (GL33/glDrawElements GL33/GL_TRIANGLES (:indices-count mesh) GL33/GL_UNSIGNED_INT 0))))
