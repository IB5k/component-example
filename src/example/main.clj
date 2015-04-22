(ns example.main
  "Main entry point"
  (:gen-class))

(defn -main [& args]
  ;; We eval so that we don't AOT anything beyond this class
  (def systems (eval '(do (require '[taoensso.encore :refer [merge-deep]])
                          (require 'example.systems.server)
                          (require 'modular.component.co-dependency)

                          (println "Starting example")
                          {:server
                           (-> (example.systems.server/new-production-system)
                               example.system/start)})))
  (println "System started"))
