(ns learnopengl.scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.model-data :as data]
            [learnopengl.camera :as camera])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl BufferUtils]
           [org.lwjgl.opengl GL33]))

(defn create-float-buffer
  [vertices]
  (doto (BufferUtils/createFloatBuffer (count vertices))
    (.put (float-array vertices))
    (.flip)))

(defn create-vertex-array
  [vertices]
  (let [vbo (GL33/glGenBuffers)
        vao (GL33/glGenVertexArrays)]
    (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER (create-float-buffer vertices) GL33/GL_STATIC_DRAW)

    (GL33/glBindVertexArray vao)

    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 12 0)
    (GL33/glEnableVertexAttribArray 0)
    vao))

(def cube-model-matrix (new Matrix4f))

(def light-model-matrix
  (doto 
    (new Matrix4f)
    (.translate (new Vector3f (float 1.2) (float 1) (float 2)))
    (.scale (new Vector3f (float 0.2)))))

(defn create
  []
  {:cube (create-vertex-array data/vertices)
   :light-cube (create-vertex-array data/vertices)
   :cube-shader (shader/get-shader-program
                  (slurp "resources/shaders/cube.vs")
                  (slurp "resources/shaders/cube.fs"))
   :light-shader (shader/get-shader-program
                   (slurp "resources/shaders/light.vs")
                   (slurp "resources/shaders/light.fs"))})

(defn render
  [scene delta]
  (let [cube (:cube scene)
        light-cube (:light-cube scene)
        cube-shader (:cube-shader scene)
        light-shader (:light-shader scene)]
    (camera/move delta)

    (GL33/glUseProgram cube-shader)
    (shader/load-vector3 cube-shader "objectColor" 1 0.5 0.31)
    (shader/load-vector3 cube-shader "lightColor" 1 1 1)

    (shader/load-matrix cube-shader "projection" (camera/perspective))
    (shader/load-matrix cube-shader "view" (camera/view))
    (shader/load-matrix cube-shader "model" cube-model-matrix)

    (GL33/glBindVertexArray cube)
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36)

    (GL33/glUseProgram light-shader)
    (shader/load-matrix light-shader "projection" (camera/perspective))
    (shader/load-matrix light-shader "view" (camera/view))
    (shader/load-matrix light-shader "model" light-model-matrix)

    (GL33/glBindVertexArray light-cube)
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36)))
