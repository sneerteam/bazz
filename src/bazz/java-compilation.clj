(ns bazz.java-compilation
  (:require [clojure.java.io :as io])
  (:import [javax.tools ToolProvider]))

;(def src-folder (io/file "/home/klaus/git/javatari/src"))
;(compile-java src-folder)

(defn compile-java [src-folder]
  (let [compiler (. ToolProvider getSystemJavaCompiler)
        main (io/file src-folder "Main.java")]
    (with-open [file-manager (. compiler (getStandardFileManager nil nil nil))]
      (.. compiler
          (getTask nil nil nil
                   ["-sourcepath" (.getAbsolutePath src-folder)]
                   nil
                   (. file-manager getJavaFileObjectsFromFiles [main]))
          call))))