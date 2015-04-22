(ns example.systems.client
  (:require [example.utils.ctr :as ctr]
            [example.utils.config :refer [config]]
            [example.utils.system :refer [expand make-system-map all-using all-used-by merge-deps start]]
            [quile.component
             :as component :refer [Lifecycle system-map system-using using]]
            [schema.core :as s :include-macros true]
            [shodan.console :as c :include-macros true]))

(enable-console-print!)

(def components
  {})

(defn new-dependency-map
  [system]
  {})

(defn new-production-system
  []
  (let [system (->> components
                    (map second)
                    (apply merge)
                    (make-system-map (config)))]
    (-> system
        (system-using (new-dependency-map system)))))

(defn start [system]
  (expand system {:before-start [[utils/validate-class]]
                  :after-start []}))

(defn main []
  (try (-> (new-production-system)
           (start))
       (catch ExceptionInfo e
         (c/log (.-cause e))
         (c/log (clj->js (.-data e)))
         (c/log (prn-str (.-data e)))
         (if-let [c (ex-cause e)]
           (throw c)))))
