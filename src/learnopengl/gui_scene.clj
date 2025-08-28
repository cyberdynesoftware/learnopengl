(ns learnopengl.gui-scene
  (:require [learnopengl.shader :as shader]
            [learnopengl.error :as error])
  (:import [org.lwjgl.util.freetype FreeType FT_Face]
           [org.lwjgl.opengl GL33]
           [org.lwjgl BufferUtils]
           [org.joml Matrix4f]))

(defn load-face
  [^String path]
  (let [library (BufferUtils/createPointerBuffer 1)
        library-error (FreeType/FT_Init_FreeType library)]
    (if (not= library-error FreeType/FT_Err_Ok)
      (println (format "Error initializing FreeType library: %d" library-error))
      (let [face-pointer (BufferUtils/createPointerBuffer 1)
            face-error (FreeType/FT_New_Face (.get library 0) path 0 face-pointer)]
        (if (not= face-error FreeType/FT_Err_Ok)
          (println (format "Error loading face: %d" face-error))
          (FT_Face/create (.get face-pointer 0)))))))

(defn load-texture
  [bitmap width height]
  (let [texture (GL33/glGenTextures)]
    (GL33/glBindTexture GL33/GL_TEXTURE_2D texture)
    (GL33/glTexImage2D GL33/GL_TEXTURE_2D
                       0
                       GL33/GL_RED
                       width
                       height
                       0
                       GL33/GL_RED
                       GL33/GL_UNSIGNED_BYTE
                       bitmap)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_S GL33/GL_CLAMP_TO_EDGE)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_T GL33/GL_CLAMP_TO_EDGE)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MIN_FILTER GL33/GL_LINEAR)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MAG_FILTER GL33/GL_LINEAR)
    texture))

(defn load-char
  [face c]
  (let [char-error (FreeType/FT_Load_Char face ^char c FreeType/FT_LOAD_RENDER)]
    (when (not= char-error FreeType/FT_Err_Ok)
      (println (format "Error loading char %d" char-error)))))

(defn create-glyph
  [face c]
  (load-char face c)
  (let [width (.. face (glyph) (bitmap) (width))
        height (.. face (glyph) (bitmap) (rows))
        pitch (.. face (glyph) (bitmap) (pitch))
        bitmap (.. face (glyph) (bitmap) (buffer (* height pitch)))]
    {:texture (load-texture bitmap width height)
     :width width
     :height height
     :bearing-left (.. face (glyph) (bitmap_left))
     :bearing-top (.. face (glyph) (bitmap_top))
     :advance (/ (.. face (glyph) (advance) (x)) 64)}))

(def projection (doto (new Matrix4f)
                  (.setOrtho2D 0 400 0 300)))

(defn create
  []
  (GL33/glPixelStorei GL33/GL_UNPACK_ALIGNMENT 1)
  (GL33/glEnable GL33/GL_BLEND)
  (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE_MINUS_SRC_ALPHA)
  (let [shader (shader/get-shader-program
                 (slurp "resources/shaders/text.vert")
                 (slurp "resources/shaders/text.frag"))
        vbo (GL33/glGenBuffers)
        vao (GL33/glGenVertexArrays)]
    (GL33/glUseProgram shader)
    (shader/load-matrix shader "projection" projection)
    (shader/load-int shader "text" 0)

    (GL33/glBindVertexArray vao)
    (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER (* 4 24) GL33/GL_DYNAMIC_DRAW)
    (GL33/glEnableVertexAttribArray 0)
    (GL33/glVertexAttribPointer 0 4 GL33/GL_FLOAT false 16 0)

    (let [face (load-face "resources/arial.ttf")]
      (FreeType/FT_Set_Pixel_Sizes face 0 16)
      {:font              (->> (range (.num_glyphs face))
                               (mapv #(create-glyph face %)))
       :num-glyphs (.num_glyphs face)
       :shader shader
       :vao vao
       :vbo vbo})))

(defn create-vertex-buffer
  [x y glyph]
  (let [xpos (+ x (:bearing-left glyph))
        ypos (- y (- (:height glyph) (:bearing-top glyph)))
        xwpos (+ xpos (:width glyph))
        yhpos (+ ypos (:height glyph))]
    (doto (BufferUtils/createFloatBuffer 24)
      (.put (float-array [xpos yhpos 0 0]))
      (.put (float-array [xpos ypos 0 1]))
      (.put (float-array [xwpos ypos 1 1]))
      (.put (float-array [xpos yhpos 0 0]))
      (.put (float-array [xwpos ypos 1 1]))
      (.put (float-array [xwpos yhpos 1 0]))
      (.flip))))

(defn render-text
  [gui x y text]
  (loop [text text
         x x]
    (let [i (int (first text))
          rest-text (rest text)
          glyph (get (:font gui) i)]
      (GL33/glBindTexture GL33/GL_TEXTURE_2D (:texture glyph))
      (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER (:vbo gui))
      (GL33/glBufferSubData GL33/GL_ARRAY_BUFFER 0 (create-vertex-buffer x y glyph))
      (GL33/glDrawArrays GL33/GL_TRIANGLES 0 6)
      (when (not-empty rest-text)
        (recur rest-text (+ x (:advance glyph)))))))

(defn font-string
  [start end]
  (apply str (map char (range start end))))

(defn render
  [gui delta]
  (let [shader (:shader gui)]
    (GL33/glUseProgram shader)
    (shader/load-matrix shader "projection" projection)
    (shader/load-float3 shader "textColor" 0 1 0)
    (GL33/glBindVertexArray (:vao gui))
    (GL33/glActiveTexture GL33/GL_TEXTURE0)
    (render-text gui 8 8 (format "FPS: %d" (int (/ 1 delta))))
    (loop [y 600
           i 0]
      (if (< (+ i 64) (:num-glyphs gui))
        (do
          (render-text gui 0 y (font-string i (+ i 64)))
          (recur (- y 20) (+ i 64)))
        (render-text gui 0 y (font-string i (:num-glyphs gui)))))))
