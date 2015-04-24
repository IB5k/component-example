(ns example.systems.client
  (:require [example.utils.config :refer [config]]
            [example.utils.system :refer [new-system start]]
            [shodan.console :as c :include-macros true]))

(defn components [config]
  {})

(defn new-production-system
  []
  (new-system (components (config))))

(defn main []
  (try (-> (new-production-system)
           (start))
       (catch ExceptionInfo e
         (c/log (.-cause e))
         (c/log (clj->js (.-data e)))
         (c/log (prn-str (.-data e)))
         (if-let [c (ex-cause e)]
           (throw c)))))
