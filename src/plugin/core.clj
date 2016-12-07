(ns plugin.core
  (:require [alembic.still :as alembic]
            [classlojure.core :as cl]
            [clojure.test :as t]))

(defn run-specter-tests
  "Should always return {:a {:aa 2} :b {:ba 0 :bb 3}}"
  [version]
  (let [fresh-classloader (apply cl/classlojure (alembic/classpath-urls))
        alembic-still (alembic/make-still fresh-classloader)
        modify-classloader! (alembic/distill [['com.rpl/specter version]]
                                             :still (atom alembic-still))]
    (cl/eval-in (:classloader alembic-still)
                `(do
                   (use 'com.rpl.specter)
                   (~'transform [~'MAP-VALS ~'MAP-VALS]
                    inc
                    {:a {:aa 1} :b {:ba -1 :bb 2}})))))

(def versions ["0.11.0" "0.11.1" "0.11.2"
               "0.12.0"
               "0.13.0" "0.13.1"])


(defn try-it-out
  [specter-test-fn]
  (mapv (fn [ver] {:version ver
                                 :result (try
                                           (= {:a {:aa 2} :b {:ba 0 :bb 3}}
                                              (specter-test-fn ver))
                                           (catch Exception e
                                             (str (.getCause e))))}) versions))

[{:version "0.11.0",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.11.1",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.11.2",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.12.0",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.13.0", :result true}
 {:version "0.13.1", :result true}]

#_(with-out-str (time (try-it-out run-specter-tests)))

"Loaded dependencies:
[[riddley \"0.1.12\"] [com.rpl/specter \"0.11.0\"]]
Loaded dependencies:
[[riddley \"0.1.12\"] [com.rpl/specter \"0.11.1\"]]
Loaded dependencies:
[[riddley \"0.1.12\"] [com.rpl/specter \"0.11.2\"]]
Loaded dependencies:
[[riddley \"0.1.12\"] [com.rpl/specter \"0.12.0\"]]
Loaded dependencies:
[[riddley \"0.1.12\"] [com.rpl/specter \"0.13.0\"]]
Loaded dependencies:
[[riddley \"0.1.12\"] [com.rpl/specter \"0.13.1\"]]
\"Elapsed time: 45642.173793 msecs\"
"
;; => 45.6 seconds for 6 versions??? Yikes!!!
;; Can we share classloaders?

(def baked-classloader (apply cl/classlojure (alembic/classpath-urls)))

(defn run-specter-tests-2
  "Should always return {:a {:aa 2} :b {:ba 0 :bb 3}}"
  [version]
  (let [alembic-still (alembic/make-still baked-classloader)
        modify-classloader! (alembic/distill [['com.rpl/specter version]]
                                             :still (atom alembic-still))]
    (cl/eval-in (:classloader alembic-still)
                `(do
                   (use 'com.rpl.specter)
                   (~'transform [~'MAP-VALS ~'MAP-VALS]
                    inc
                    {:a {:aa 1} :b {:ba -1 :bb 2}})))))

(try-it-out run-specter-tests-2)
[{:version "0.11.0",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.11.1",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.11.2",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.12.0",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.13.0",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}
 {:version "0.13.1",
  :result
  "java.lang.RuntimeException: Unable to resolve symbol: transform in this context, compiling:(NO_SOURCE_PATH:0:0)"}]

;; Well that bombed...So what went wrong?

"Loaded dependencies:
[[riddley  0.1.12] [com.rpl/specter  0.11.0]]
WARN: com.rpl/specter version 0.11.1 requested, but 0.11.0 already on classpath.
Loaded dependencies:
[[riddley  0.1.12]]
Dependencies not loaded due to conflict with previous jars :
[[com.rpl/specter  0.11.1]]
WARN: com.rpl/specter version 0.11.2 requested, but 0.11.0 already on classpath.
Loaded dependencies:
[[riddley  0.1.12]]
Dependencies not loaded due to conflict with previous jars :
[[com.rpl/specter  0.11.2]]
WARN: com.rpl/specter version 0.12.0 requested, but 0.11.0 already on classpath.
Loaded dependencies:
[[riddley  0.1.12]]
Dependencies not loaded due to conflict with previous jars :
[[com.rpl/specter  0.12.0]]
WARN: com.rpl/specter version 0.13.0 requested, but 0.11.0 already on classpath.
Loaded dependencies:
[[riddley  0.1.12]]
Dependencies not loaded due to conflict with previous jars :
[[com.rpl/specter  0.13.0]]
WARN: com.rpl/specter version 0.13.1 requested, but 0.11.0 already on classpath.
Loaded dependencies:
[[riddley  0.1.12]]
Dependencies not loaded due to conflict with previous jars :
[[com.rpl/specter  0.13.1]]"

;; The version didn't get loaded due to a conflict.
;; This means we can't share classloaders :(


