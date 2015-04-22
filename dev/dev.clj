(ns dev
  (:require [dev-components.visualization :refer (new-system-visualizer)]
            [example.systems.server :refer :all]
            [example.utils.config :refer [config]]
            [example.utils.system :refer [make-system-map all-using all-used-by merge-deps]]
            [com.stuartsierra.component :as component :refer [system-map system-using using]]
            [modular.component.co-dependency :refer (co-using system-co-using)]
            [example.utils.maker :refer [make]]))

(defn visualization [system config]
  (assoc system
         :visualization (make new-system-visualizer config
                              :output-dir "./viz"
                              :options {:dpi 100}
                              :system (new-production-system)
                              :clusters (->> (for [[cluster cmps] components]
                                               [(str cluster)
                                                (make-system-map config cmps)])
                                             (into {})))))

(defn new-development-system
  []
  (let [config (config)
        s-map (-> (new-system-map config)
                  (visualization config))]
    (-> s-map
        (system-using (new-dependency-map s-map))
        (system-co-using (new-co-dependency-map s-map)))))
