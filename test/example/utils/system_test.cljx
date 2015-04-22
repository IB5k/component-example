(ns example.utils.system-test
  (:require [example.utils.system :refer [start]]
            [#+clj  com.stuartsierra.component
                #+cljs quile.component
                :as component :refer [system-map system-using using]]))

#+clj
(defmacro with-system [name f & body]
  `(do
     (def ~name (example.utils.system/start (~f)))
     (let [result# (do ~@body)]
       (com.stuartsierra.component/stop ~name)
       result#)))
