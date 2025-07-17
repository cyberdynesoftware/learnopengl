(ns learnopengl.opengl-helper
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl BufferUtils]
           [org.lwjgl.stb STBImage]
           [org.lwjgl.glfw GLFW]
           [org.lwjgl.opengl GL33]))

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

(defn create-vertex-array-with-normals-and-tex-coords
  []
  (let [vao (GL33/glGenVertexArrays)]
    (GL33/glBindVertexArray vao)
    (GL33/glVertexAttribPointer 0 3 GL33/GL_FLOAT false 32 0)
    (GL33/glEnableVertexAttribArray 0)
    (GL33/glVertexAttribPointer 1 3 GL33/GL_FLOAT false 32 12)
    (GL33/glEnableVertexAttribArray 1)
    (GL33/glVertexAttribPointer 2 2 GL33/GL_FLOAT false 32 24)
    (GL33/glEnableVertexAttribArray 2)
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
