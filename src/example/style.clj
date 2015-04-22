(ns example.style
  (:refer-clojure :exclude [+ - * /])
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [px]]
            [garden.core :refer [css]]
            [garden.color :refer [hsl rgb]]
            [garden.arithmetic :refer [+ - * /]]
            [garden.stylesheet :refer [at-media]]
            [clojure.string :as str]))

(defstyles style
  [:body
   {:margin "0px"}])
