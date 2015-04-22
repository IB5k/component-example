(ns dev-components.visualization
  (:require [detached.utils.component :refer :all]
            [clojure.pprint :refer (pprint)]
            [clojure.string :as str]
            [com.stuartsierra.component :as component :refer (Lifecycle)]
            [me.raynes.fs :as fs]
            [plumbing.core :refer :all :exclude (update)]
            [rhizome.viz :as viz]
            [schema.core :as s]))

(defn remove-key-remapping [deps]
  (map-vals (partial mapv second) deps))

(defn invert-graph [g]
  (or (->> (for [[k vs] g]
             (zipmap vs (repeat [k])))
           (apply merge-with (comp vec concat)))
      {}))

(defn get-deps [system]
  (map-vals (comp #(if (nil? %) {} %)
                  (partial apply merge {})
                  (juxt :com.stuartsierra.component/dependencies
                        :modular.component.co-dependency/co-dependencies)
                  meta)
            system))

(defn format-system-graph [system]
  (->> system
       (get-deps)
       (remove-key-remapping)
       (invert-graph)))

(defn get-all-keys [graph]
  (set (concat (keys graph)
               (mapcat second graph))))

(defn narrow-system [system cluster]
  (->> cluster
       (format-system-graph)
       (get-all-keys)
       (select-keys system)))

(defn format-cluster-mapping [system clusters]
  (->> clusters
       (map-vals (comp keys
                       (partial narrow-system system)))
       (invert-graph)))

(def colors-10 ["#1f77b4"
                "#ff7f0e"
                "#2ca02c"
                "#d62728"
                "#9467bd"
                "#8c564b"
                "#e377c2"
                "#7f7f7f"
                "#bcbd22"
                "#17becf"])

(defn category-scale [categories]
  (let [remaining (atom categories)
        assignments (atom {})]
    (fn [input]
      (if-let [assigned (get @assignments input)]
        assigned
        (let [[category & cs] (some-> remaining deref shuffle)]
          (assert category "more inputs than categories!")
          (reset! remaining cs)
          (swap! assignments assoc input category)
          category)))))

(defn viz-system [f system clusters & [{:as options} & {:as opts}]]
  (let [g (format-system-graph system)
        color-scale (category-scale colors-10)]
    (apply f (get-all-keys g) g
           :vertical? false
           :node->descriptor (fn [n] {:label n})
           :cluster->descriptor (fn [n]
                                  {:label (str/join ", " n)
                                   ;; :color (color-scale n)
                                   })
           :node->cluster (format-cluster-mapping system clusters)
           :options options
           (mapcat identity opts))))

(defn save-graph [system clusters options filename]
      (viz-system viz/save-graph system clusters options
                  :filename filename))

(s/defrecord SystemVisualizer
    [system :- {s/Keyword s/Any}
     clusters :- {s/Str {s/Keyword s/Any}}
     output-dir :- s/Str
     options :- {s/Keyword s/Any}]
  Lifecycle
  (start [this]
    (fs/delete-dir output-dir)
    (fs/mkdirs output-dir)
    (save-graph system {}
                options (str output-dir "/system.png"))
    (save-graph system clusters
                options (str output-dir "/system_clusters.png"))
    (let [output-dir (str output-dir "/clusters")]
      (fs/mkdirs output-dir)
      (doseq [[name cluster] clusters]
        (save-graph (narrow-system system cluster) {}
                    options (str output-dir "/" name ".png"))))
    this)
  (stop [this] this))

(def new-system-visualizer
  (-> map->SystemVisualizer
      (wrap-class-validation SystemVisualizer)
      (wrap-defaults {:system {}
                      :clusters {}
                      :options {}
                      :output-dir "./resources/viz"})
      (wrap-kargs)))
