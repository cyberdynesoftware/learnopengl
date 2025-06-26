(ns learnopengl.scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.model-data :as data]
            [learnopengl.camera :as camera])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl BufferUtils]
           [org.lwjgl.stb STBImage]
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
    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 32 0)
    (GL33/glEnableVertexAttribArray 0)
    (when load-normals
      (GL33/glVertexAttribPointer 1 3 GL33/GL_FLOAT false 32 12)
      (GL33/glEnableVertexAttribArray 1)
      (GL33/glVertexAttribPointer 2 2 GL33/GL_FLOAT false 32 24)
      (GL33/glEnableVertexAttribArray 2))
    vao))

(defn load-texture
  [path]
  (STBImage/stbi_set_flip_vertically_on_load true)
  (let [texture (GL33/glGenTextures)
        width (BufferUtils/createIntBuffer 1)
        height (BufferUtils/createIntBuffer 1)
        channels (BufferUtils/createIntBuffer 1)
        image-data (STBImage/stbi_load path width height channels 0)
        image-format (condp = (.get channels)
                       3 GL33/GL_RGB
                       4 GL33/GL_RGBA
                       nil)]
    (GL33/glBindTexture GL33/GL_TEXTURE_2D texture)
    (GL33/glTexImage2D GL33/GL_TEXTURE_2D
                       0
                       image-format
                       (.get width)
                       (.get height)
                       0
                       image-format
                       GL33/GL_UNSIGNED_BYTE
                       image-data)
    (GL33/glGenerateMipmap GL33/GL_TEXTURE_2D)

    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_S GL33/GL_REPEAT)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_T GL33/GL_REPEAT)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MIN_FILTER GL33/GL_LINEAR_MIPMAP_LINEAR)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MAG_FILTER GL33/GL_LINEAR)

    (STBImage/stbi_image_free image-data)
    texture))

(defn create
  []
  (create-vertex-buffer data/vertices-with-normals-and-tex-coords)
  (let [cube (create-vertex-array true)
        light-cube (create-vertex-array false)
        cube-shader (shader/get-shader-program
                      (slurp "resources/shaders/cube.vert")
                      (slurp "resources/shaders/cube.frag"))
        light-shader (shader/get-shader-program
                       (slurp "resources/shaders/light.vert")
                       (slurp "resources/shaders/light.frag"))
        diffuse-texture (load-texture "resources/assets/container2.png")
        specular-texture (load-texture "resources/assets/container2_specular.png")]
    (GL33/glUseProgram cube-shader)
    (shader/load-int cube-shader "material.diffuse" 0)
    (shader/load-int cube-shader "material.specular" 1)
    {:cube cube
     :light-cube light-cube
     :cube-shader cube-shader
     :light-shader light-shader
     :diffuse-texture diffuse-texture
     :specular-texture specular-texture}))

(def cube-model-matrix (new Matrix4f))

(def light-model-matrix (new Matrix4f))

(defn update-light-model-matrix
  []
  (.translation light-model-matrix light-position)
  (.scale light-model-matrix (new Vector3f (float 0.2))))

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

    (shader/load-float1 cube-shader "material.shininess" 64)

    (shader/load-vector3 cube-shader "light.position" light-position)
    (shader/load-float3 cube-shader "light.ambient" 0.2 0.2 0.2)
    (shader/load-float3 cube-shader "light.diffuse" 0.5 0.5 0.5)
    (shader/load-float3 cube-shader "light.specular" 1 1 1)

    (GL33/glActiveTexture GL33/GL_TEXTURE0)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:diffuse-texture scene))

    (GL33/glActiveTexture GL33/GL_TEXTURE1)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:specular-texture scene))

    (GL33/glBindVertexArray cube)
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36)

    (GL33/glUseProgram light-shader)

    (shader/load-matrix light-shader "projection" (camera/perspective))
    (shader/load-matrix light-shader "view" (camera/view))
    (shader/load-matrix light-shader "model" (update-light-model-matrix))
    (shader/load-vector3 light-shader "lightColor" light-color)

    (GL33/glBindVertexArray light-cube)
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 36)))
