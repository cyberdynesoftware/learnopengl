(defproject learnopengl "0.1.0-SNAPSHOT"
  :description "learnopengl book code"
  :url "learnopengl.com"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.lwjgl/lwjgl "3.3.6"]
                 [org.lwjgl/lwjgl "3.3.6" :classifier "natives-linux"]
                 [org.lwjgl/lwjgl-opengl "3.3.6"]
                 [org.lwjgl/lwjgl-opengl "3.3.6" :classifier "natives-linux"]
                 [org.lwjgl/lwjgl-stb "3.3.6"]
                 [org.lwjgl/lwjgl-stb "3.3.6" :classifier "natives-linux"]
                 [org.lwjgl/lwjgl-glfw "3.3.6"]
                 [org.lwjgl/lwjgl-glfw "3.3.6" :classifier "natives-linux"]
                 [org.joml/joml "1.10.8"]]
  :main ^:skip-aot learnopengl.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
