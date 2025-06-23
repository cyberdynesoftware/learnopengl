(ns learnopengl.input
  (:require [learnopengl.camera :as camera])
  (:import [org.lwjgl.glfw GLFW GLFWKeyCallbackI]))

(def key-callback (reify GLFWKeyCallbackI
                    (invoke [_ window keycode _ action _]
                      (when (= action GLFW/GLFW_PRESS)
                        (condp = keycode
                          GLFW/GLFW_KEY_ESCAPE (GLFW/glfwSetWindowShouldClose window true)
                          GLFW/GLFW_KEY_W (reset! camera/direction :forward)
                          GLFW/GLFW_KEY_S (reset! camera/direction :backward)
                          GLFW/GLFW_KEY_A (reset! camera/direction :left)
                          GLFW/GLFW_KEY_D (reset! camera/direction :right)
                          nil))
                      (when (= action GLFW/GLFW_RELEASE)
                        (reset! camera/direction nil)))))
