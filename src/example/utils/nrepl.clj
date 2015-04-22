(ns example.utils.nrepl
  (:require [example.utils.ctr :as ctr]
            [cemerick.piggieback]
            [cider.nrepl :refer [cider-nrepl-handler cider-middleware]]
            [cljx.repl-middleware]
            [clojure.tools.nrepl.server :as nrepl-server]
            [com.stuartsierra.component :refer (Lifecycle)]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(defn start-nrepl [port host handler]
  (log/info (str "nREPL listening on: " host ":" port))
  (nrepl-server/start-server :port port
                             :bind host
                             :handler handler))

(s/defrecord NREPLServer
    [port :- s/Int
     host :- s/Str
     middleware :- [s/Symbol]]
  Lifecycle
  (start [this]
    (let [server (start-nrepl port host (apply nrepl-server/default-handler (map resolve middleware)))]
      (assoc this
             :server server)))
  (stop [this]
    (some-> this :server nrepl-server/stop-server)
    this))

(def new-nrepl-server
  (-> map->NREPLServer
      (ctr/wrap-class-validation NREPLServer)
      (ctr/wrap-defaults {:port 3001
                            :host "0.0.0.0"
                            :middleware (concat cider.nrepl/cider-middleware
                                                '[cljx.repl-middleware/wrap-cljx
                                                  cemerick.piggieback/wrap-cljs-repl])})
      (ctr/wrap-kargs)))
