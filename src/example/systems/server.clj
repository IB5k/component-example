(ns example.systems.server
  (:require [example.utils.config :refer [config]]
            [example.utils.maker :refer [make]]
            [example.utils.system :refer [new-system]]
            [bidi.bidi :refer (RouteProvider)]
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
  (new-system (components (config))))
