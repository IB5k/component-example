(ns example.systems.client
  (:require [example.utils.config :refer [config]]
            [example.utils.system :refer [expand]]
            [ib5k.component.ctr :as ctr]
            [ib5k.component.using-schema :refer [system-using-schema]]
            [plumbing.core :refer [map-vals]]
            [quile.component
             :as component :refer [Lifecycle system-map system-using using]]
            [schema.core :as s :include-macros true]
            [shodan.console :as c :include-macros true]))

(defn components []
  {})

(defn new-production-system
  []
  (new-system (components (config))))
(enable-console-print!)

(defn components [config]
  {})

(defn new-production-system
  []
  (let [components (components (config))
        system (->> components
                    (map-vals :cmp)
                    (apply concat)
                    (apply system-map))
        using (->> components
                   (map-vals :using)
                   (remove (comp nil? second))
                   (into {}))]
    (system-using-schema system using)))

(defn start [system]
  (expand system {:before-start [[ctr/validate-class]]
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
