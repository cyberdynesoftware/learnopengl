(ns learnopengl.core
  (:require [learnopengl.scene :as scene]
            [learnopengl.input :as input]
            [learnopengl.camera :as camera]
            [learnopengl.mesh-model :as mesh])
  (:import [org.lwjgl.glfw GLFW]
           [org.lwjgl.opengl GL GL33]
           [org.lwjgl.system MemoryUtil])
  (:gen-class))

(def foo (atom (mesh/read-model "resources/assets/backpack/backpack.obj")))
(println (count @foo))

(def last-frame (atom 0))

(defn move-camera
  [window delta]
  (when (= (GLFW/glfwGetKey window GLFW/GLFW_KEY_W) GLFW/GLFW_PRESS)
    (camera/move :front 1 delta))
  (when (= (GLFW/glfwGetKey window GLFW/GLFW_KEY_S) GLFW/GLFW_PRESS)
    (camera/move :front -1 delta))
  (when (= (GLFW/glfwGetKey window GLFW/GLFW_KEY_A) GLFW/GLFW_PRESS)
    (camera/move :right -1 delta))
  (when (= (GLFW/glfwGetKey window GLFW/GLFW_KEY_D) GLFW/GLFW_PRESS)
    (camera/move :right 1 delta)))

(defn -main
  "learnopengl"
  [& args]
  (GLFW/glfwInit)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MAJOR 3)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MINOR 3)
  (GLFW/glfwWindowHint GLFW/GLFW_OPENGL_PROFILE GLFW/GLFW_OPENGL_CORE_PROFILE)

  (let [window (GLFW/glfwCreateWindow (int 400) (int 300) "learnopengl" MemoryUtil/NULL MemoryUtil/NULL)]
    (GLFW/glfwMakeContextCurrent window)
    (GL/createCapabilities)

    (GL33/glEnable GL33/GL_DEPTH_TEST)
    (GLFW/glfwSetInputMode window GLFW/GLFW_CURSOR GLFW/GLFW_CURSOR_DISABLED)

    (camera/update-front-right-up)
    (GLFW/glfwSetCursorPosCallback window camera/cursor-callback)
    (GLFW/glfwSetScrollCallback window camera/scroll-callback)
    (GLFW/glfwSetKeyCallback window input/key-callback)

    (let [scene (scene/create)]
      (while (not (GLFW/glfwWindowShouldClose window))
        (let [now (GLFW/glfwGetTime)
              delta (- now @last-frame)]
          (reset! last-frame now)
          (move-camera window delta)

          (GL33/glClearColor (float 0.1) (float 0.1) (float 0.1) (float 1))
          (GL33/glClear (bit-or GL33/GL_COLOR_BUFFER_BIT GL33/GL_DEPTH_BUFFER_BIT))

          (scene/render scene delta)

          (GLFW/glfwSwapBuffers window)
          (GLFW/glfwPollEvents)))))

  (GLFW/glfwTerminate)
  (println "So long..."))
