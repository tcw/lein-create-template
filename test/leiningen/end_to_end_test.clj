(ns leiningen.end_to_end_test
  (:use clojure.test
        leiningen.create-template)
  (:require [clojure.java.io :as jio]
            [leiningen.file-utils :as fu]))

(def test-dir (fu/create-tmp-dir "/dev" 10))

(def new-template-name "my-new-template")

(defn mock-file [relative-file-name]
  (jio/as-file (str test-dir relative-file-name)))

(def mock-files
  {:project-file   (mock-file "/project.clj")
   :source-file1   (mock-file "/src/my_skeleton/core.clj")
   :source-file2   (mock-file "/src/my_skeleton/core_utils_2.clj")
   :test-file1     (mock-file "/test/my_skeleton/core_test.clj")
   :resource-file1 (mock-file "/resources/index-1.html")
   :resource-file2 (mock-file "/resources/index_2.html")})

(defn dev-resource [name]
  (jio/as-file (jio/resource name)))

(defn add-test-dir [path]
  (str test-dir path))

(defn build-mock-project []
  (fu/copy-file-force-path (dev-resource "dev_project.clj") (:project-file mock-files))
  (fu/copy-file-force-path (dev-resource "dev_core.clj") (:source-file1 mock-files))
  (fu/copy-file-force-path (dev-resource "dev_core_utils_2.clj") (:source-file2 mock-files))
  (fu/copy-file-force-path (dev-resource "dev_core_test.clj") (:test-file1 mock-files))
  (fu/copy-file-force-path (dev-resource "index_1.html") (:resource-file1 mock-files))
  (fu/copy-file-force-path (dev-resource "index-2.html") (:resource-file2 mock-files)))

(def mock-project
  {:name           "my-skeleton",
   :root           test-dir,
   :source-paths   [(add-test-dir "/src")],
   :resource-paths [(add-test-dir "/resources")],
   :test-paths     [(add-test-dir "/test")]})

(deftest end-to-end
  (build-mock-project)
  (is (jio/as-file fu/tmp-dir))
  (is (re-seq #"/dev[0-9]*$" (str test-dir)))
  (is (re-seq #"dev_core.clj" (str (jio/resource "dev_core.clj"))))
  (is (= "Created template project:my-new-template\n"
         (with-out-str (create-template mock-project new-template-name))))
  (fu/delete-file-recursively test-dir))

(run-tests 'leiningen.end_to_end_test)