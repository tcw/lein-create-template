(ns leiningen.end_to_end_test_spec
  (:use speclj.core
        leiningen.create-template)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs]
            [leiningen.file-utils :as fu])
  (:import (java.io File FileNotFoundException)))

(def test-dir (fu/create-tmp-dir "/dev" 10))

(def new-template-name "my-new-template")

(defn mock-file [relative-file-name]
  (jio/as-file (str test-dir relative-file-name)))

(def mock-files
  {:project-file (mock-file "/project.clj")
   :source-file1 (mock-file "/src/my_skeleton/core.clj")
   :source-file2 (mock-file "/src/my_skeleton/core_utils_2.clj")
   :test-file1 (mock-file "/test/my_skeleton/core_test.clj")
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
  {:name "my-skeleton",
   :root test-dir,
   :source-paths [(add-test-dir "/src")],
   :resource-paths [(add-test-dir "/resources")],
   :test-paths [(add-test-dir "/test")]})

(defn get-files-by-ending [files file-name]
  (filter #(not= (re-seq (re-pattern (str file-name "$")) (str %)) nil) files))


(describe "Verify preconditions"

  (after-all (build-mock-project))

  (it "makes sure java.io.tmpdir exists"
    (should (.exists (jio/as-file fu/tmp-dir))))

  (it "makes sure skeleton test dir has been created"
    (should (re-seq #"/dev[0-9]*$" (str test-dir))))

  (it "makes sure development resources are available"
    (should (re-seq #"dev_core.clj" (str (jio/resource "dev_core.clj"))))))


(describe "Running End-to-End"

  (it "runs lein-create-template without new template name"
    (should= "Can't create template, there already exists a folder named: \n"
      (with-out-str (create-template mock-project ""))))

  (it "runs lein-create-template with new template name"
    (should= "Created template project:my-new-template\n"
      (with-out-str (create-template mock-project new-template-name))))

  (it "runs lein-create-template again and warns that a project already exists"
    (should= "Can't create template, there already exists a folder named: my-new-template\n"
      (with-out-str (create-template mock-project new-template-name)))))


(describe "Verifies expectations of result from End-to-End"

  (after-all (fu/delete-file-recursively test-dir))

  (with files (fu/walk (jio/as-file (str test-dir "/" new-template-name))))

  (it "checks that all files where created "
    (should= 8 (count @files)))

  (it "checks that lein newnew project file is as expected"
    (should= 1 (count (get-files-by-ending @files "my-new-template/project.clj")))
    (should-not= nil (re-seq #"lein-template" (slurp (first (get-files-by-ending @files "my-new-template/project.clj"))))))

  (it "checks that skeleton project file is as expected"
    (should= 1 (count (get-files-by-ending @files "my_new_template/project.clj")))
    (let [project-file (slurp (first (get-files-by-ending @files "my_new_template/project.clj")))]
      (should= 1 (count (re-seq #"\(defproject \{\{ns-name\}\}" project-file)))
      (should= 1 (count (re-seq #"\[\{\{ns-name\}\}/something" project-file)))))

  (it "checks that the template render file is as expected"
    (should= 1 (count (get-files-by-ending @files "new/my_new_template.clj")))
    (let [render-file (slurp (first (get-files-by-ending @files "new/my_new_template.clj")))]
        (should= 3 (count (re-seq #"\{\{sanitized\}\}" render-file)))
        (should= 3 (count (re-seq #"my-new-template" render-file)))
        (should= 2 (count (re-seq #"core.clj" render-file)))
        (should= 2 (count (re-seq #"core_utils_2.clj" render-file)))
        (should= 2 (count (re-seq #"core_test.clj" render-file)))
        (should= 2 (count (re-seq #"project.clj" render-file)))
        (should= 1 (count (re-seq #"index-1.html" render-file)))
        (should= 1 (count (re-seq #"index_1.html" render-file)))
        (should= 2 (count (re-seq #"index_2.html" render-file)))
        (should= 6 (count (re-seq #"data" render-file)))))

  (it "checks that clojure files are as expected"
    (should= 1 (count (get-files-by-ending @files "my_new_template/core.clj")))
    (should-not= nil (re-seq #"\(ns \{\{ns-name\}\}.core\)" (slurp (first (get-files-by-ending @files "my_new_template/core.clj")))))
    (should= 1 (count (get-files-by-ending @files "my_new_template/core_utils_2.clj")))
    (should-not= nil (re-seq #"\(ns \{\{ns-name\}\}.core-utils-2" (slurp (first (get-files-by-ending @files "my_new_template/core_utils_2.clj"))))))

  (it "checks that all resource files are as expected"
    (should= 1 (count (get-files-by-ending @files "my_new_template/index_1.html")))
    (let [resource1 (slurp (first (get-files-by-ending @files "my_new_template/index_1.html")))]
      (should-not= nil (re-seq #"my-skeleton" resource1))
      (should-not= nil (re-seq #"my_skeleton" resource1)))
    (should= 1 (count (get-files-by-ending @files "my_new_template/index_2.html")))
    (let [resource2 (slurp (first (get-files-by-ending @files "my_new_template/index_2.html")))]
    (should-not= nil (re-seq #"my-skeleton" resource2))
    (should-not= nil (re-seq #"my_skeleton" resource2))))

  )

(describe "Verify postconditions"

  (it "makes sure all test resources have been deleted"
    (should-not (.exists test-dir)))
  )


(run-specs)