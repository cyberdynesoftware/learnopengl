(ns learnopengl.error
  (:import [org.lwjgl.opengl GL33]))

(def error-codes
  {GL33/GL_INVALID_ENUM "INVALID_ENUM"
   GL33/GL_INVALID_VALUE "INVALID_VALUE"
   GL33/GL_INVALID_OPERATION "INVALID_OPERATION"
   GL33/GL_STACK_OVERFLOW "STACK_OVERFLOW"
   GL33/GL_STACK_UNDERFLOW "STACK_UNDERFLOW"
   GL33/GL_OUT_OF_MEMORY "OUT_OF_MEMORY"
   GL33/GL_INVALID_FRAMEBUFFER_OPERATION "INVALID_FRAMEBUFFER_OPERATION"})

(defn check-error
  []
  (let [error-code (GL33/glGetError)]
    (when (not= error-code GL33/GL_NO_ERROR)
      (println (format "OpenGL error: %s" (error-codes error-code))))))
