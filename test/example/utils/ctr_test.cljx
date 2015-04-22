(ns example.utils.ctr-test
  #+clj
  (:require [example.utils.ctr :as ctr]
            [clojure.set :as set]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [schema.core :as s])
  #+cljs
  (:require [example.utils.ctr :as ctr]
            [clojure.set :as set]
            [cemerick.cljs.test :as t]
            [cljs.test.check.cljs-test :refer-macros (defspec)]
            [cljs.test.check :as tc]
            [cljs.test.check.generators :as gen]
            [cljs.test.check.properties :as prop :include-macros true]
            [schema.core :as s :include-macros true])
  #+cljs
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)]))

(deftest empty-kargs
  (let [f (ctr/wrap-kargs identity)]
    (is (= {} (f)))
    (is (= {} (f nil)))
    (is (= {} (f {})))))

(defspec kargs-vs-map
  10
  (let [f (ctr/wrap-kargs identity)]
    (prop/for-all [v (gen/map gen/any gen/any)]
                  (= (f v)
                     (apply f (apply concat v))))))

(s/defrecord TestRecord [num :- s/Num
                         str :- s/Str])

(with-test (def new-test-record
             (-> map->TestRecord
                 (ctr/wrap-class-validation TestRecord)
                 (ctr/wrap-defaults {:num 0})
                 (ctr/wrap-kargs)))
  (is (= (:num (new-test-record))
         0))
  (is (not (:str (new-test-record))))
  (is (thrown? #+clj Exception #+cljs js/Error
               (-> (new-test-record)
                   (ctr/validate-class))))
  (is (-> (new-test-record :str "string")
          (ctr/validate-class))))