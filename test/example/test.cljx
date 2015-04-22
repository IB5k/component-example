(ns example.test
  (:require [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component :refer [system-map system-using using]])
  #+cljs
  (:require-macros [example.test]))

(def ^:dynamic *system* nil)

#+clj
(defmacro with-system
  [start system & body]
  `(let [s# (~start ~system)]
     (try
       (binding [*system* s#] ~@body)
       (finally
         (component/stop s#)))))

(defn with-system-fixture
  [start system]
  (fn [f]
    (with-system (system)
      (f))))
