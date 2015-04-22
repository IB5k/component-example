(ns example.utils.system
  (:require [example.utils.ctr :refer [validate-class]]
            [example.utils.maker :refer (make)]
            [#+clj  com.stuartsierra.component
             #+cljs quile.component
             :as component :refer [system-map system-using using]]
            #+clj  [tangrammer.component.co-dependency :as co-dep]
            #+clj  [plumbing.core :refer :all]
            #+cljs [plumbing.core :refer (map-vals) :refer-macros (defnk fnk)]
            #+clj [clojure.java.io :as io]
            [schema.core :as s]
            [taoensso.encore :refer [merge-deep]]
            #+clj  [taoensso.timbre :as log]
            #+cljs [shodan.console :as c :include-macros true]))

#+clj
(log/set-level! :debug)
#+cljs
(enable-console-print!)

;; ========== Deps ==========

(defn make-dep-map [key-or-map]
  (cond-> key-or-map
    (keyword? key-or-map) (hash-map key-or-map)))

(defn all-using [dependency cmp-keys]
  (zipmap cmp-keys (repeat (make-dep-map dependency))))

(defn all-used-by [cmp dependencies]
  (some->> dependencies
           (map make-dep-map)
           (reduce merge)
           (hash-map cmp)))

(defn merge-deps [& deps]
  (reduce merge-deep deps))

;; ========== Makers ==========

(defn maker [config]
  (fnk make-component [ctr {opts {}} {using {}} #+clj {co-using {}}]
    (let [cmp (try (if (seq opts)
                     (apply make ctr config
                            (apply concat opts))
                     (ctr))
                   (catch #+clj Exception #+cljs js/Error e
                          (throw (ex-info (str "failed to make component: " ctr)
                                          (merge {:ctr ctr
                                                  :opts opts
                                                  :using using
                                                  :error e}
                                                 #+clj {:co-using co-using}
                                                 {:config config})))))]
      (-> cmp
          (component/using using)
          #+clj (co-dep/co-using co-using)))))

(defn make-system-map
  [config components]
  (apply system-map
         (->> components
              (map-vals (maker config))
              (apply concat))))

;; ========== Start ==========

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
