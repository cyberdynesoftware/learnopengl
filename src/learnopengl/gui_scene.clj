(ns learnopengl.gui-scene
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
  (let [face (load-face "resources/arial.ttf")]
    (->> (range 128)
         (map (fn [c]
                (load-char face c)
                (let [width (.. face (glyph) (bitmap) (width))
                      height (.. face (glyph) (bitmap) (rows))
                      bitmap (.. face (glyph) (bitmap) (buffer height (.. face (glyph) (bitmap) (pitch))))]
                  {:texture (load-texture bitmap width height)
                   :width width
                   :height height
                   :bitmap-left (.. face (glyph) (bitmap_left))
                   :bitmap-top (.. face (glyph) (bitmap_top))
                   :advance (.. face (glyph) (advance) (x))}))))

    (println (.num_glyphs face))
    (println (.num_faces face))
    ))

(defn render
  [gui delta]
  )
