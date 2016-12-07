(require '[boot.pod :as pod])

(def versions ["0.11.0" "0.11.1" "0.11.2"
               "0.12.0"
               "0.13.0" "0.13.1"])
(clojure.pprint/pprint
 {"RESULTS!!!!"
  (mapv (fn [v]
          (try (pod/with-eval-in (pod/make-pod
                                  (update-in (get-env) [:dependencies]
                                             conj ['com.rpl/specter v]))
                 (use 'com.rpl.specter)
                 (= {:a {:aa 2} :b {:ba 0 :bb 3}}
                    (transform [MAP-VALS MAP-VALS]
                               inc
                               {:a {:aa 1} :b {:ba -1 :bb 2}})))
               (catch Exception e
                 (str (.getCause e)))))
        versions)})

