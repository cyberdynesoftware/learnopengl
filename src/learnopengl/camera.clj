(ns learnopengl.camera
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.glfw GLFWCursorPosCallbackI GLFWScrollCallbackI]))

(def position (new Vector3f (float 0) (float 0) (float 3)))

(def front (new Vector3f (float 0) (float 0) (float -1)))
(def front-temp (new Vector3f))

(def world-up (new Vector3f (float 0) (float 1) (float 0)))
(def up (new Vector3f world-up))

(def right (new Vector3f))
(def right-temp (new Vector3f))

(def direction (atom nil))

(def last-x (atom 200))
(def last-y (atom 150))
(def yaw (atom -90))
(def pitch (atom 0))
(def first-call (atom true))

(defn constrain-pitch
  [pitch]
  (if (> pitch 89)
    89
    (if (< pitch -89)
      -89
      pitch)))

(defn update-front-right-up
  []
  (.normalize
    (.set front
          (float (* (Math/cos (Math/toRadians @yaw))
                    (Math/cos (Math/toRadians @pitch))))
          (float (Math/sin (Math/toRadians @pitch)))
          (float (* (Math/sin (Math/toRadians @yaw))
                    (Math/cos (Math/toRadians @pitch))))))
  (.set front-temp front)
  (.set right (.normalize (.cross front-temp world-up)))
  (.set right-temp right)
  (.set up (.normalize (.cross right-temp front))))

(def cursor-callback
  (reify GLFWCursorPosCallbackI
    (invoke [_ _ x y]
      (when @first-call
        (reset! last-x x)
        (reset! last-y y)
        (reset! first-call false))
      (let [sensitivity 0.1
            offset-x (* (- x @last-x) sensitivity)
            offset-y (* (- y @last-y) sensitivity -1)]
        (reset! last-x x)
        (reset! last-y y)
        (swap! yaw + offset-x)
        (swap! pitch + offset-y)
        (swap! pitch constrain-pitch))
      (update-front-right-up))))

(defn move
  [delta]
  (let [speed 2.5]
    (.set front-temp front)
    (.set right-temp right)
    (condp = @direction
      :forward (.add position (.mul (.normalize front-temp) (float (* speed delta))))
      :backward (.add position (.mul (.normalize front-temp) (float (* speed delta -1))))
      :left (.add position (.mul (.normalize right-temp) (float (* speed delta -1))))
      :right (.add position (.mul (.normalize right-temp) (float (* speed delta))))
      nil)))

(def view-matrix (new Matrix4f))

(defn view
  []
  (.set front-temp front)
  (.setLookAt view-matrix position (.add front-temp position) up))

(def fov (atom 45))

(def scroll-callback
  (reify GLFWScrollCallbackI
    (invoke [_ _ _ y]
      (swap! fov - (* y 0.1))
      (swap! fov #(if (< % 1)
                    1
                    (if (> % 45)
                      45
                      %))))))

(def perspective-matrix (new Matrix4f))

(defn perspective
  []
  (.setPerspective perspective-matrix (float @fov) (float (/ 4 3)) (float 0.1) (float 100)))
