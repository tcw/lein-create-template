(ns leiningen.create-template-test
  (:use clojure.test
        leiningen.create-template)
  (:require [clojure.java.io :as jio]))

(def root-path "/home/user/leiningen/tempalte/")

(defn mock-file [relative-file-name]
  (jio/as-file (str root-path relative-file-name)))

(def source-file (mock-file "/src/rest_ful.clj"))

(def resource-file (mock-file "/resources/index-site.html"))

(def mock-info
  {:root-path        root-path
   :old-project-name "rest-ful"
   :new-project-name "rest-ful-template"
   :project-file     (mock-file "/project.clj")
   :source-files     (seq [source-file
                           (mock-file "/src/rest_ful_2.clj")
                           (mock-file "/src/rest_ful_2.cljs")
                           (mock-file "/src/sub/rest_ful_2.cljs")
                           (mock-file "/template/stencil.mustache")])
   :resource-files   (seq [resource-file (mock-file "/resources/some_crazy-File.js")])
   :java-files       (seq [(mock-file "/java/rest_ful_test.java")
                           (mock-file "/java/rest_ful_2_test.java")])
   :test-files       (seq [(mock-file "/spec/rest_ful_spec.clj")
                           (mock-file "/spec/rest_ful_2_spec.clj")])})
