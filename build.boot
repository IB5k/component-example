(defn make-korks [korks]
  (cond-> korks
    (keyword? korks) vector))

(defn flatten-vals
  "takes a hashmap and recursively returns a flattened list of all the values"
  [coll]
  (if ((every-pred coll? sequential?) coll)
    coll
    (mapcat flatten-vals (vals coll))))

(defn build-deps [deps & korks]
  (->> korks
       (mapv (comp (partial get-in deps) make-korks))
       (mapcat flatten-vals)
       (into [])))

(def deps '{:async         [[org.clojure/core.async "0.1.346.0-17112a-alpha"]]
            :clojure       [[org.clojure/clojure "1.7.0-beta1"]]
            :clojurescript [[org.clojure/clojurescript "0.0-3196"]]
            :component
            {:clj          [[com.stuartsierra/component "0.2.3"]
                            [juxt.modular/co-dependency "0.2.0"]]
             :cljs         [[quile/component-cljs "0.2.4"]]}
            :filesystem
            {:io           [[me.raynes/fs "1.4.6"]]}
            :garden        [[garden "1.2.5"]]
            :logging
            {:clj          [[com.taoensso/timbre "3.4.0"]]
             :cljs         [[shodan "0.4.1"]]}
            :repl          [[com.cemerick/piggieback "0.2.0"]
                            [org.clojure/tools.namespace "0.2.10"]
                            [org.clojure/tools.nrepl "0.2.10"]
                            [weasel "0.6.0"]
                            [cider/cider-nrepl "0.9.0-SNAPSHOT"]
                            [com.keminglabs/cljx "0.6.0"]]
            :schema        [[prismatic/plumbing "0.4.2"]
                            [prismatic/schema "0.4.0"]]
            :http          [[ring "1.3.2"]
                            [juxt.modular/bidi "0.9.2"]
                            [juxt.modular/http-kit "0.5.4"]]
            :reader        [[org.clojure/tools.reader "0.9.1"]]
            :viz           [[rhizome "0.2.4"]]})

(set-env!
 :dependencies (vec
                (concat
                 (apply build-deps deps (keys deps))
                 (mapv #(conj % :scope "test")
                       '[[org.clojure/test.check "0.7.0"]
                         [com.cemerick/clojurescript.test "0.3.3"]
                         [adzerk/bootlaces "0.1.11"]
                         [adzerk/boot-cljs "0.0-2814-4"]
                         [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT"]
                         [pandeiro/boot-http "0.6.2"]
                         [adzerk/boot-reload "0.2.6"]
                         [adzerk/boot-test "1.0.4"]
                         [boot-cljs-test/node-runner "0.1.0"]
                         [boot-garden "1.2.5-2"]
                         [deraen/boot-cljx "0.2.2"]
                         [jeluard/boot-notify "0.1.2"]
                         [pleasetrythisathome/boot-component "0.1.2-SNAPSHOT"]])))
 :source-paths #{"src"}
 :resource-paths #(conj % "resources"))

(require
 '[adzerk.bootlaces           :refer :all]
 '[adzerk.boot-cljs           :refer :all]
 '[adzerk.boot-cljs-repl      :refer :all]
 '[adzerk.boot-reload         :refer :all]
 '[adzerk.boot-test           :refer [test]]
 '[boot-cljs-test.node-runner :refer :all]
 '[boot-garden.core           :refer :all]
 '[boot-component.reloaded    :refer :all]
 '[deraen.boot-cljx           :refer :all]
 '[jeluard.boot-notify        :refer :all]
 '[pandeiro.boot-http         :refer [serve]])

(def +version+ "0.1.0-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom {:project 'pleasetrythisathome/component-example
      :version +version+
      :description "Component system example repo with all the trimmings"
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
      :url "https://github.com/pleasetrythisathome/component-example"
      :scm {:url "https://github.com/pleasetrythisathome/component-example"}}
 aot {:namespace #{'example.main}}
 jar {:main 'example.main
      :file "example.jar"}
 cljs-test-node-runner {:namespaces '[example.main-test]}
 cljs {:output-to "public/js/main.js"
       :compiler-options {:warnings {:single-segment-namespace false}}}
 garden {:styles-var 'example.style/style
         :output-to "public/css/style.css"
         :vendors ["webkit" "moz" "o"]})

(deftask test-all
  "test clj and cljs"
  []
  (set-env! :source-paths #(conj % "test"))
  (comp
   (cljx)
   (cljs-test-node-runner)
   (cljs :source-map true
         :pretty-print true)
   (test)
   (run-cljs-test)))

(deftask test-auto []
  (set-env! :source-paths #(conj % "test"))
  (comp
   (watch)
   (notify)
   (cljx)
   (cljs-test-node-runner)
   (cljs :source-map true
         :optimizations :none)
   (test)
   (run-cljs-test)))

(deftask dev
  "watch and compile cljx, css, cljs, init cljs-repl and push changes to browser"
  []
  (set-env! :source-paths #(conj % "dev" "test"))
  (set-env! :resource-paths #(conj % "src" "test"))
  (comp
   (watch)
   (notify)
   (cljx)
   (reload :port 3449)
   (cljs-repl :port 3448)
   (reload-system :system-var 'dev/new-development-system
                  :start-var 'example.utils.system/start)
   (reload-system-cljs :system-var 'example.systems.client/new-production-system
                       :start-var 'example.utils.system/start)
   (garden :pretty-print true)
   (cljs :source-map true
         :pretty-print true)))

(deftask package
  "compile cljx, garden, cljs, and build a jar"
  []
  (comp
   (cljx)
   (garden)
   (cljs :optimizations :advanced)
   (aot)
   (pom)
   (uber)
   (jar)))
