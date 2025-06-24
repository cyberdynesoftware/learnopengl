(ns learnopengl.input
  (:require [learnopengl.camera :as camera])
  (:import [org.lwjgl.glfw GLFW GLFWKeyCallbackI]))

(def key-callback (reify GLFWKeyCallbackI
                    (invoke [_ window keycode _ action _]
                      (when (= action GLFW/GLFW_PRESS)
                        (condp = keycode
                          GLFW/GLFW_KEY_ESCAPE (GLFW/glfwSetWindowShouldClose window true)
                          nil)))))
