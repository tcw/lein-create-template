(ns leiningen.sanitize-utils-test
  (:use clojure.test leiningen.sanitize-utils)
  (:require [clojure.java.io :as jio]))

(deftest sanitizes-a-clojure-namespace-to-file-name
  (is (= "create_template" (sanitize-from-clj "create-template")))
  (is (= "create_template_more" (sanitize-from-clj "create-template-more")))
  (is (= "createless" (sanitize-from-clj "createless")))
  (is (= "create_less" (sanitize-from-clj "create_less"))))

(deftest creates-a-relative-file-path-for-a-file-from-an-absolute-root-path
  (is (= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/user")))
  (is (not (= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/use")))))

(deftest creates-a-relative-path-for-files-in-the-new-lein-template-project
  (is (= "home/user/mytemplates/ringtemplate/src/leiningen/new/ringtemplate/" (new-lein-path "home/user/mytemplates" "ringtemplate"))))
