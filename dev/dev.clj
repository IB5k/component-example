(ns dev
  (:require [dev-components.visualization :refer (new-system-visualizer)]
            [example.systems.server :refer (new-production-system)]
            [example.utils.config :refer [config]]
            [example.utils.maker :refer [make]]))

(defn visualization [system config]
  (assoc system
         :visualization (make new-system-visualizer config
                              :output-dir "./viz"
                              :options {:dpi 100}
                              :system system)))

(defn new-development-system
  []
  (let [config (config)]
    (-> (new-production-system)
        (visualization config))))
