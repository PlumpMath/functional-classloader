(ns alembic-example
  (:require [alembic.still :as alembic]
            [classlojure.core :as cl]
            [clojure.test :as t]
            [clojure.string :as str]))

;; Code to fmap over nested values, dynamically loading the version of specter
(defn get-code-to-fmap-nested-values
  [specter-version fn-to-apply nested-map]
  (cond
    (str/starts-with? specter-version "0.13")
    (concat `(do)
            (list
             `(use 'com.rpl.specter)
             (list 'transform ['MAP-VALS 'MAP-VALS]
                   fn-to-apply nested-map)))
    :else
    (concat `(do)
            (list
             `(use 'com.rpl.specter)
             `(use 'com.rpl.specter.macros)
             (list 'transform ['MAP-VALS 'MAP-VALS]
                   fn-to-apply nested-map)))))

(get-code-to-fmap-nested-values "0.13.1" 'inc {:a {:aa 2} :b {:ba 0 :bb 3}})
(get-code-to-fmap-nested-values "0.12.0" 'dec {:a {:aa 2} :b {:ba 0 :bb 3}})

(defn fmap-nested-with-specter-version
  [specter-version fn-to-apply nested-map]
  (let [code-to-eval (get-code-to-fmap-nested-values specter-version fn-to-apply nested-map)
        fresh-classloader (apply cl/classlojure (alembic/classpath-urls))
        alembic-still (alembic/make-still fresh-classloader)
        modify-classloader! (alembic/distill [['com.rpl/specter specter-version]]
                                             :still (atom alembic-still))]
    (cl/eval-in (:classloader alembic-still)
                code-to-eval)))

(fmap-nested-with-specter-version "0.13.1" 'inc {:a {:b 2} :c {:d 3 :e 4}})
(fmap-nested-with-specter-version "0.12.0" 'inc {:a {:b 2} :c {:d 3 :e 4}})
(fmap-nested-with-specter-version "0.11.1" 'inc {:a {:b 2} :c {:d 3 :e 4}})
