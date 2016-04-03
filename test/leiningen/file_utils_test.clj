(ns leiningen.file-utils-test
  (:use clojure.test)
  (:require [leiningen.file-utils :as fu]))

(deftest generates-a-new-template-clj-project-file
  (let [template (fu/create-project-template "rest-ful-template")]
    (is (re-seq #"ct rest-ful-template/lein-t" template))
    (is (re-seq #"eval-in-leiningen true\)" template))))