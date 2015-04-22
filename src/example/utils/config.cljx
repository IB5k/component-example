(ns example.utils.config
  #+clj
  (:require [taoensso.encore :refer [merge-deep]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.reader.reader-types :refer [indexing-push-back-reader]]
            [clojure.tools.reader :as reader]
            [taoensso.timbre :as log])
  #+cljs
  (:require [taoensso.encore :refer [merge-deep]]
            [cljs.reader :as edn]
            [goog.dom :as gdom]
            [shodan.console :as c :include-macros true])
  #+cljs
  (:require-macros [example.utils.config :refer [config-from-classpath]]))

#+clj
(defn ^:private read-file
  [f]
  (reader/read
   ;; This indexing-push-back-reader gives better information if the
   ;; file is misconfigured.
   (indexing-push-back-reader
    (java.io.PushbackReader. (io/reader f)))))

#+clj
(defn ^:private config-from
  [f]
  (if (.exists f)
    (read-file f)
    {}))

#+clj
(defn ^:private user-config
  []
  (config-from (io/file (System/getProperty "user.home") ".example.edn")))

#+cljs
(defn ^:private user-config
  []
  {})

#+clj
(defmacro config-from-classpath []
  (if-let [res (io/resource "example.edn")]
    (config-from (io/file res))
    {}))

(defn config
  []
  (merge-deep (config-from-classpath)
              (user-config)))
