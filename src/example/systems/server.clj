(ns example.systems.server
  (:require [example.utils.config :refer [config]]
            [example.utils.nrepl]
            [example.utils.system :refer [make-system-map all-using all-used-by merge-deps]]
            [com.stuartsierra.component :refer [system-map system-using]]
            [modular.bidi]
            [modular.component.co-dependency :refer (system-co-using)]
            [modular.http-kit]
            [modular.ring :refer (WebRequestMiddleware)]))

(defrecord ExampleMiddleware []
  WebRequestMiddleware
  (request-middleware [_] identity))

(def components
  {:dev
   {:nrepl
    {:ctr example.utils.nrepl/new-nrepl-server
     :opts {{:port [:nrepl :port]} 3001}}}
   :http
   {:public-resources
    {:ctr modular.bidi/new-web-resources
     :opts {:uri-context "/public"
            :resource-prefix "public"}}
    :webrouter
    {:ctr modular.bidi/new-router
     :using [:public-resources]}
    :webhead
    {:ctr modular.ring/new-web-request-handler-head
     :using {:request-handler :webrouter}}
    :webserver
    {:ctr modular.http-kit/new-webserver
     :opts {{:port [:web :port]} 3000}
     :using [:webhead]}}})

(defn new-dependency-map
  [system]
  (merge-deps
   (all-used-by :webhead
                (for [[k cmp] system
                      :when (satisfies? WebRequestMiddleware cmp)]
                  k))
   (all-used-by :webrouter
                (for [[k cmp] (dissoc system :webrouter)
                      :when (satisfies? RouteProvider cmp)]
                  k))))

(defn new-co-dependency-map
  [system]
  {})

(defn new-system-map
  [config]
  (->> components
       (map second)
       (apply merge)
       (make-system-map config)))

(defn new-production-system
  []
  (let [system (new-system-map (config))]
    (-> system
        (system-using (new-dependency-map system))
        (system-co-using (new-co-dependency-map system)))))
