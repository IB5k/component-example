(ns example.utils.system
  (:require [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component :refer [system-map system-using using]]))

;; taken from https://github.com/milesian/BigBang/blob/master/src/milesian/bigbang.clj
(defn expand
  [system-map {:keys [before-start after-start]}]
  (let [on-start-sequence (apply conj before-start (cons [component/start] after-start))
        start (fn [c & args]
                (apply (->> on-start-sequence
                            (mapv (fn [[f & args]]
                                    #(apply f (conj args %))))
                            reverse
                            (apply comp))
                       (conj args c)))]
    (component/update-system system-map (keys system-map) start)))
