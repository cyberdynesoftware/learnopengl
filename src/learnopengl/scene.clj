(ns learnopengl.scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.model-data :as data]
            [learnopengl.camera :as camera])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl BufferUtils]
           [org.lwjgl.glfw GLFW]
           [org.lwjgl.opengl GL33]))

(def light-position (new Vector3f (float 1.2) (float 1) (float 2)))
(def light-color (new Vector3f (float 1) (float 1) (float 1)))

(defn create-float-buffer
  [vertices]
  (doto (BufferUtils/createFloatBuffer (count vertices))
    (.put (float-array vertices))
    (.flip)))

(defn create-vertex-buffer
  [vertices]
  (let [vbo (GL33/glGenBuffers)]
    (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER (create-float-buffer vertices) GL33/GL_STATIC_DRAW)))

(defn create-vertex-array
  [load-normals]
  (let [vao (GL33/glGenVertexArrays)]
    (GL33/glBindVertexArray vao)
    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 24 0)
    (GL33/glEnableVertexAttribArray 0)
    (when load-normals
      (GL33/glVertexAttribPointer 1 3 GL33/GL_FLOAT false 24 12)
      (GL33/glEnableVertexAttribArray 1))
    vao))

(def cube-model-matrix (new Matrix4f))

(def light-model-matrix (new Matrix4f))

(defn update-light-model-matrix
  []
  (.translation light-model-matrix light-position)
  (.scale light-model-matrix (new Vector3f (float 0.2))))

(defn create
  []
  (create-vertex-buffer data/vertices-with-normals)
  {:cube (create-vertex-array true)
   :light-cube (create-vertex-array false)
   :cube-shader (shader/get-shader-program
                  (slurp "resources/shaders/cube.vs")
                  (slurp "resources/shaders/cube.fs"))
   :light-shader (shader/get-shader-program
                   (slurp "resources/shaders/light.vs")
                   (slurp "resources/shaders/light.fs"))})

(defn rotate-light
  []
  (let [t (GLFW/glfwGetTime)
        radius 2]
    (.set light-position
          (float (* (Math/sin t) radius))
          (float 1)
          (float (* (Math/cos t) radius)))))

(defn render
  [scene delta]
  (let [cube (:cube scene)
        light-cube (:light-cube scene)
        cube-shader (:cube-shader scene)
        light-shader (:light-shader scene)]
    (rotate-light)

    (GL33/glUseProgram cube-shader)

    (shader/load-matrix cube-shader "projection" (camera/perspective))
    (shader/load-matrix cube-shader "view" (camera/view))
    (shader/load-matrix cube-shader "model" cube-model-matrix)
    (shader/load-vector3 cube-shader "viewPos" camera/position)

    (shader/load-float3 cube-shader "material.ambient" 0.24725 0.1995 0.0745)
    (shader/load-float3 cube-shader "material.diffuse" 0.75164 0.60648 0.22648)
    (shader/load-float3 cube-shader "material.specular" 0.628281 0.555802 0.366065)
    (shader/load-float1 cube-shader "material.shininess" (* 0.4 128))

    (shader/load-vector3 cube-shader "light.position" light-position)
    (shader/load-vector3 cube-shader "light.diffuse" light-color)
    (shader/load-vector3 cube-shader "light.ambient" light-color)
    (shader/load-float3 cube-shader "light.specular" 1 1 1)

    (GL33/glBindVertexArray cube)
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36)

    (GL33/glUseProgram light-shader)
    (shader/load-matrix light-shader "projection" (camera/perspective))
    (shader/load-matrix light-shader "view" (camera/view))
    (shader/load-matrix light-shader "model" (update-light-model-matrix))
    (shader/load-vector3 light-shader "lightColor" light-color)

    (GL33/glBindVertexArray light-cube)
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36)))
