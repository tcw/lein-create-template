(ns leiningen.create-template-spec
  (:use speclj.core
        leiningen.create-template)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs]))

(def root-path "/home/user/leiningen/tempalte/")

(defn mock-file [relative-file-name]
  (jio/as-file (str root-path relative-file-name)))

(def source-file (mock-file "/src/rest_ful.clj"))

(def resource-file (mock-file "/resources/index-site.html"))

(def mock-info
  {:root-path root-path
   :old-project-name "rest-ful"
   :new-project-name "rest-ful-template"
   :project-file (mock-file "/project.clj")
   :source-files (seq [source-file (mock-file "/src/rest_ful_2.clj") (mock-file "/src/rest_ful_2.cljs") (mock-file "/template/stencil.mustache")])
   :resource-files (seq [resource-file (mock-file "/resources/some_crazy-File.js")])
   :java-files (seq [(mock-file "/java/rest_ful_test.java") (mock-file "/java/rest_ful_2_test.java")])
   :test-files (seq [(mock-file "/spec/rest_ful_spec.clj") (mock-file "/spec/rest_ful_2_spec.clj")])})

(describe "File and namespace utils"

  (it "sanitizes a clojure namespace to file name"
    (should= "create_template" (sanitize-from-clj "create-template"))
    (should= "create_template_more" (sanitize-from-clj "create-template-more"))
    (should= "createless" (sanitize-from-clj "createless"))
    (should= "create_less" (sanitize-from-clj "create_less")))

  (it "sanitizes a file name to clojure namespace"
    (should= "create-template" (sanitize-to-clj "create_template"))
    (should= "create-template-more" (sanitize-to-clj "create_template_more"))
    (should= "createless" (sanitize-to-clj "createless"))
    (should= "create-less" (sanitize-to-clj "create-less")))

  (it "sanitizes a project name to lein-newnew template form"
    (should= "leiningen.new.{{sanitized}}" (sanitize-project-name "leiningen.new.mytemplate" "mytemplate"))
    (should= "leiningen.new.{{sanitized}}.other" (sanitize-project-name "leiningen.new.mytemplate.other" "mytemplate"))
    (should= "{{sanitized}}" (sanitize-project-name "mytemplate" "mytemplate")))

  (it "creates a relative file path for a file from an absolute root path"
    (should= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/user"))
    (should-not= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/use")))

  (it "creates a relative path for files in the new lein template project"
    (should= "home/user/mytemplates/ringtemplate/src/leiningen/new/ringtemplate/" (new-lein-path "home/user/mytemplates" "ringtemplate")))

  (it "creates a file with sanitized file name"
    (should= "home/user/lein/index_site.html" (str (new-file-path resource-file "home/user/lein/"))))

  (it "takes a file from the skeleton project and returns a file for the template project"
    (should= "/home/user/leiningen/tempalte/ringtemplate/src/leiningen/new/ringtemplate/rest_ful.clj" (str (get-new-sanitized-lein-file source-file root-path "ringtemplate"))))

  (it "desides if a file is of type"
    (should (is-file-type "clj" source-file))
    (should (is-file-type "html" resource-file))
    (should-not (is-file-type "clj" resource-file)))

  )

(describe "Organizing files from skeleton project"

  (it "returns a set of all files"
    (should= 11 (count (get-all-files mock-info))))

  (it "returns all clj and cljs files"
    (should= 6 (count (get-all-clj-files mock-info))))

  )

(describe "Generating clj code files"

  (it "generates a new clj code line that points to the file"
    (should= "[\"/home/user/leiningen/tempalte/src/rest_ful.clj\" (render \"rest_ful.clj\" data)]\n" (make-file-line source-file root-path (:new-project-name mock-info) true))
    (should= "[\"/home/user/leiningen/tempalte/resources/index-site.html\" (render \"index_site.html\")]\n" (make-file-line resource-file root-path (:new-project-name mock-info) false)))

  (it "generates a new template clj code file that specifies how to render resources"
    (should (re-seq #"\[\"/home/user/leiningen/tempalte/resources/index-site\.html"
              (create-template-render-file (:source-files mock-info) (:resource-files mock-info) mock-info))))

  (it "generates a new template clj project file"
    (let [template (create-project-template-file mock-info)]
      (should (re-seq #"ct rest-ful-template/lein-t" template))
      (should (re-seq #"eval-in-leiningen true\)" template))))

  )

(run-specs)