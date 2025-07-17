(ns learnopengl.lights
  (:require [learnopengl.shader :as shader])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(def light-color (new Vector3f (float 1) (float 1) (float 1)))

(def spotlight-cut-off (Math/cos (Math/toRadians 12.5)))
(def spotlight-outer-cut-off (Math/cos (Math/toRadians 17.5)))

(defn init-directional-light
  [shader-program]
  (shader/load-float3 shader-program "dirLight.direction" -0.2 -1 -0.3)
  (shader/load-float3 shader-program "dirLight.ambient" 0.05 0.05 0.05)
  (shader/load-float3 shader-program "dirLight.diffuse" 0.4 0.4 0.4)
  (shader/load-float3 shader-program "dirLight.specular" 0.5 0.5 0.5))

(defn init-point-lights
  [shader-program num-lights positions]
  (doseq [index (range num-lights)]
    (shader/load-vector3 shader-program
                         (format "pointLights[%d].position" index)
                         (get positions index))

    (shader/load-float3 shader-program (format "pointLights[%d].ambient" index) 0.05 0.05 0.05)
    (shader/load-float3 shader-program (format "pointLights[%d].diffuse" index) 0.8 0.8 0.8)
    (shader/load-float3 shader-program (format "pointLights[%d].specular" index) 1 1 1)

    (shader/load-float1 shader-program (format "pointLights[%d].constant" index) 1)
    (shader/load-float1 shader-program (format "pointLights[%d].linear" index) 0.09)
    (shader/load-float1 shader-program (format "pointLights[%d].quadratic" index) 0.032)))

(defn init-spotlight
  [shader-program]
  (shader/load-float1 shader-program "spotLight.cutOff" spotlight-cut-off)
  (shader/load-float1 shader-program "spotLight.outerCutOff" spotlight-outer-cut-off)

  (shader/load-float3 shader-program "spotLight.ambient" 0 0 0)
  (shader/load-float3 shader-program "spotLight.diffuse" 1 1 1)
  (shader/load-float3 shader-program "spotLight.specular" 1 1 1)

  (shader/load-float1 shader-program "spotLight.constant" 1)
  (shader/load-float1 shader-program "spotLight.linear" 0.09)
  (shader/load-float1 shader-program "spotLight.quadratic" 0.032))
