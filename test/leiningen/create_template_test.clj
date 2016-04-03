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
   :source-files     (seq [source-file (mock-file "/src/rest_ful_2.clj") (mock-file "/src/rest_ful_2.cljs") (mock-file "/template/stencil.mustache")])
   :resource-files   (seq [resource-file (mock-file "/resources/some_crazy-File.js")])
   :java-files       (seq [(mock-file "/java/rest_ful_test.java") (mock-file "/java/rest_ful_2_test.java")])
   :test-files       (seq [(mock-file "/spec/rest_ful_spec.clj") (mock-file "/spec/rest_ful_2_spec.clj")])})

(deftest sanitizes-a-clojure-namespace-to-file-name
  (is (= "create_template" (sanitize-from-clj "create-template")))
  (is (= "create_template_more" (sanitize-from-clj "create-template-more")))
  (is (= "createless" (sanitize-from-clj "createless")))
  (is (= "create_less" (sanitize-from-clj "create_less"))))

(deftest sanitizes-a-file-name-to-clojure-namespace
  (is (= "create-template" (sanitize-to-clj "create_template")))
  (is (= "create-template-more" (sanitize-to-clj "create_template_more")))
  (is (= "createless" (sanitize-to-clj "createless")))
  (is (= "create-less" (sanitize-to-clj "create-less"))))

(deftest sanitizes-a-project-name-to-lein-newnew-template-form
  (is (= "leiningen.new.{{sanitized}}" (sanitize-project-name "leiningen.new.mytemplate" "mytemplate")))
  (is (= "leiningen.new.{{sanitized}}.other" (sanitize-project-name "leiningen.new.mytemplate.other" "mytemplate")))
  (is (= "{{sanitized}}" (sanitize-project-name "mytemplate" "mytemplate"))))

(deftest creates-a-relative-file-path-for-a-file-from-an-absolute-root-path
  (is (= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/user")))
  (is (not (= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/use")))))

(deftest creates-a-relative-path-for-files-in-the-new-lein-template-project
  (is (= "home/user/mytemplates/ringtemplate/src/leiningen/new/ringtemplate/" (new-lein-path "home/user/mytemplates" "ringtemplate"))))

(deftest creates-a-file-with-sanitized-file-name
  (is (= "home/user/lein/index_site.html" (str (new-file-path resource-file "home/user/lein/")))))

(deftest takes-a-file-from-the-skeleton-project-and-returns-a-file-for-the-template-project
  (is (= "/home/user/leiningen/tempalte/ringtemplate/src/leiningen/new/ringtemplate/rest_ful.clj" (str (get-new-sanitized-lein-file source-file root-path "ringtemplate")))))

(deftest desides-if-a-file-is-of-type
  (is (is-file-type "clj" source-file))
  (is (is-file-type "html" resource-file))
  (is (not (is-file-type "clj" resource-file))))

(deftest returns-a-set-of-all-files
  (is (= 11 (count (get-all-files mock-info)))))

(deftest returns-all-clj-and-cljs-files
  (is (= 6 (count (get-all-clj-files mock-info)))))

(deftest generates-a-new-clj-code-line-that-points-to-the-file
  (is (= "[\"/home/user/leiningen/tempalte/src/rest_ful.clj\" (render \"rest_ful.clj\" data)]\n" (make-file-line source-file root-path (:new-project-name mock-info) true)))
  (is (= "[\"/home/user/leiningen/tempalte/resources/index-site.html\" (render \"index_site.html\")]\n" (make-file-line resource-file root-path (:new-project-name mock-info) false))))

(deftest generates-a-new-template-clj-code-file-that-specifies-how-to-render-resources
  (is (re-seq #"\[\"/home/user/leiningen/tempalte/resources/index-site\.html"
              (create-template-render-file (:source-files mock-info) (:resource-files mock-info) mock-info))))