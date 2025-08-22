(ns learnopengl.gui-scene
  (:require [learnopengl.shader :as shader])
  (:import [org.lwjgl.util.freetype FreeType FT_Face]
           [org.lwjgl.opengl GL33]
           [org.lwjgl BufferUtils]))

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

(defn create
  []
  (GL33/glPixelStorei GL33/GL_UNPACK_ALIGNMENT 1)
  {:font (let [face (load-face "resources/arial.ttf")]
           (FreeType/FT_Set_Pixel_Sizes face 0 48)
           (println (.num_glyphs face))
           (->> (range 128)
                (mapv (fn [c]
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
                           :advance (.. face (glyph) (advance) (x))})))))
   :shader (shader/get-shader-program
             (slurp "resources/shaders/text.vert")
             (slurp "resources/shaders/text.frag"))
   :buffer (let [vbo (GL33/glGenBuffers)
                 vao (GL33/glGenVertexArrays)]
             (GL33/glBindVertexArray vao)
             (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
             (GL33/glBufferData GL33/GL_ARRAY_BUFFER (* 4 24) GL33/GL_DYNAMIC_DRAW)
             (GL33/glEnableVertexAttribArray 0)
             (GL33/glVertexAttribPointer 0 4 GL33/GL_FLOAT false 16 0)
             {:vao vao :vbo vbo})})

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
  [x y text]
  (doseq [c text]
    (println (int c))))

(defn render
  [gui delta]
  (render-text 100 100 "hello"))
