(ns learnopengl.scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.model-data :as data]
            [learnopengl.camera :as camera]
            [learnopengl.opengl-helper :as ogl]
            [learnopengl.lights :as lights])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(defn create-vertex-array
  []
  (let [vao (GL33/glGenVertexArrays)]
    (GL33/glBindVertexArray vao)
    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 32 0)
    (GL33/glEnableVertexAttribArray 0)
    vao))

(def cube-model-matrix (new Matrix4f))
(def light-model-matrix (new Matrix4f))
(def light-scale (new Vector3f (float 0.2)))
(def light-color (new Vector3f (float 1) (float 1) (float 1)))
(def pivot (new Vector3f (float 1) (float 0.3) (float 0.5)))

(defn create
  []
  (ogl/create-vertex-buffer data/vertices-with-normals-and-tex-coords)
  (let [cube (ogl/create-vertex-array-with-normals-and-tex-coords)
        light-cube (create-vertex-array)
        cube-shader (shader/get-shader-program
                      (slurp "resources/shaders/cube.vert")
                      (slurp "resources/shaders/cube.frag"))
        light-shader (shader/get-shader-program
                       (slurp "resources/shaders/light.vert")
                       (slurp "resources/shaders/light.frag"))
        diffuse-texture (ogl/load-texture "resources/assets/container2.png")
        specular-texture (ogl/load-texture "resources/assets/container2_specular.png")]
    (GL33/glUseProgram cube-shader)

    (shader/load-int cube-shader "material.diffuse" 0)
    (shader/load-int cube-shader "material.specular" 1)
    (shader/load-float1 cube-shader "material.shininess" 32)

    (lights/init-directional-light cube-shader)
    (lights/init-point-lights cube-shader 4 data/point-light-positions)
    (lights/init-spotlight cube-shader)

    {:cube cube
     :light-cube light-cube
     :cube-shader cube-shader
     :light-shader light-shader
     :diffuse-texture diffuse-texture
     :specular-texture specular-texture}))

(defn render
  [scene delta]
  (let [cube (:cube scene)
        light-cube (:light-cube scene)
        cube-shader (:cube-shader scene)
        light-shader (:light-shader scene)]
    (GL33/glUseProgram cube-shader)

    (shader/load-matrix cube-shader "projection" (camera/perspective))
    (shader/load-matrix cube-shader "view" (camera/view))
    (shader/load-vector3 cube-shader "viewPos" camera/position)

    (shader/load-vector3 cube-shader "spotLight.position" camera/position)
    (shader/load-vector3 cube-shader "spotLight.direction" camera/front)

    (GL33/glActiveTexture GL33/GL_TEXTURE0)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:diffuse-texture scene))

    (GL33/glActiveTexture GL33/GL_TEXTURE1)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:specular-texture scene))

    (GL33/glBindVertexArray cube)

    (doseq [[position angle] (partition 2 (interleave data/cube-positions (range 20 400 20)))]
      (.translation cube-model-matrix position)
      (.rotate cube-model-matrix (org.joml.Math/toRadians (float angle)) pivot)
      (shader/load-matrix cube-shader "model" cube-model-matrix)
      (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36))

    (GL33/glUseProgram light-shader)
    (GL33/glBindVertexArray light-cube)

    (shader/load-matrix light-shader "projection" (camera/perspective))
    (shader/load-matrix light-shader "view" (camera/view))
    (shader/load-vector3 light-shader "lightColor" light-color)

    (doseq [position data/point-light-positions]
      (.translation light-model-matrix position)
      (.scale light-model-matrix light-scale)
      (shader/load-matrix light-shader "model" light-model-matrix)
      (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36))))
