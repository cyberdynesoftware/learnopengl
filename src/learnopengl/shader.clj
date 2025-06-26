(ns learnopengl.shader
  (:import [org.lwjgl.opengl GL33]
           [org.lwjgl.system MemoryStack]))

(defn create-shader
  [shader-type source]
  (let [shader (GL33/glCreateShader shader-type)]
    (GL33/glShaderSource shader source)
    (GL33/glCompileShader shader)
    (let [success (GL33/glGetShaderi shader GL33/GL_COMPILE_STATUS)]
      (when (= success 0)
        (println "ERROR: shader compilation failed:")
        (println (GL33/glGetShaderInfoLog shader))))
    shader))

(defn create-shader-program
  [& shader]
  (let [program (GL33/glCreateProgram)]
    (dorun (map #(GL33/glAttachShader program %) shader))
    (GL33/glLinkProgram program)
    (let [success (GL33/glGetProgrami program GL33/GL_LINK_STATUS)]
      (when (= success 0)
        (println "ERROR: shader program linking failed:")
        (println (GL33/glGetProgramInfoLog program))))
    program))

(defn get-shader-program
  [vertex-shader-source
   fragment-shader-source]
  (let [vertex-shader (create-shader GL33/GL_VERTEX_SHADER vertex-shader-source)
        fragment-shader (create-shader GL33/GL_FRAGMENT_SHADER fragment-shader-source)
        shader-program (create-shader-program vertex-shader fragment-shader)]
    (GL33/glDeleteShader vertex-shader)
    (GL33/glDeleteShader fragment-shader)
    shader-program))

(defn load-matrix
  [shader-program location-id matrix]
  (with-open [stack (MemoryStack/stackPush)]
    (GL33/glUniformMatrix4fv
      (GL33/glGetUniformLocation shader-program location-id)
      false
      (.get matrix (.mallocFloat stack 16)))))

(defn load-vector3
  [shader-program location-id vector3]
  (GL33/glUniform3f
    (GL33/glGetUniformLocation shader-program location-id)
    (.x vector3)
    (.y vector3)
    (.z vector3)))

(defn load-float3
  [shader-program location-id x y z]
  (GL33/glUniform3f
    (GL33/glGetUniformLocation shader-program location-id)
    (float x)
    (float y)
    (float z)))

(defn load-float1
  [shader-program location-id value]
  (GL33/glUniform1f
    (GL33/glGetUniformLocation shader-program location-id)
    (float value)))

(defn load-int
  [shader-program location-id value]
  (GL33/glUniform1i
    (GL33/glGetUniformLocation shader-program location-id)
    (int value)))
