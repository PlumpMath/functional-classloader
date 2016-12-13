(ns classlojure-example
  (:require [classlojure.core :as cl]
            [clojure.java.io :as io]))

;; Doesn't work
#_(do (require '[cljs.core.macros])
      (ns-aliases 'cljs.core.macros))

;; Does work
(cl/eval-in (cl/classlojure "file:cljs.jar")
            `(do (require '[cljs.core.macros])
                 (ns-aliases 'cljs.core.macros)))

;; Classlojure doesn't load transitive dependencies, so your JAR must be
;; self-contained (or standalone) or you must include all dependencies


