(ns example.systems.server
  (:require [example.utils.config :refer [config]]
            [example.utils.maker :refer [make]]
            [example.utils.system :refer [expand]]
            [bidi.bidi :refer (RouteProvider)]
            [com.stuartsierra.component :refer [system-map]]
            [ib5k.component.ctr :as ctr]
            [ib5k.component.using-schema :refer [system-using-schema]]
            [milesian.identity :as identity]
            [modular.bidi]
            [modular.http-kit]
            [modular.ring :refer (WebRequestMiddleware)]
            [plumbing.core :refer :all]
            [schema.core :as s]
            [tangrammer.component.co-dependency :as co-dependency]))

(defrecord ExampleMiddleware []
  WebRequestMiddleware
  (request-middleware [_] identity))

(defn components [confg]
  {:public-resources
   {:cmp (make modular.bidi/new-web-resources config
               :uri-context "/public"
               :resource-prefix "public")}
   :webrouter
   {:cmp (modular.bidi/new-router)
    :using [:public-resources (s/protocol RouteProvider)]}
   :webhead
   {:cmp (modular.ring/new-web-request-handler-head)
    :using {:request-handler :webrouter
            (s/protocol WebRequestMiddleware) (s/protocol WebRequestMiddleware)}}
   :webserver
   {:cmp (make modular.http-kit/new-webserver config
               {:port [:web :port]} 3000)
    :using [:webhead]}})

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
  (let [system-atom (atom system)]
    (expand system {:before-start [[identity/add-meta-key system]
                                   [co-dependency/assoc-co-dependencies system-atom]
                                   [ctr/validate-class]]
                    :after-start [[co-dependency/update-atom-system system-atom]
                                  [ctr/validate-class]]})))
