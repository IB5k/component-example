(ns example.main-test
  #+cljs
  (:require [cemerick.cljs.test :refer (run-all-tests)])
  #+cljs
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)]))

#+cljs
(do
  (enable-console-print!)
  (run-all-tests))
