(ns leiningen.create-template-spec
  (:use speclj.core
        leiningen.create-template)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs]))

(def root-path "/home/user/leiningen/tempalte/")

(defn mock-file [relative-file-name]
  (jio/as-file (str root-path relative-file-name)))

(def mock-info
    {:root-path root-path
     :old-project-name "rest-ful"
     :new-project-name "rest-ful-template"
     :project-file (mock-file "/project.clj")
     :source-files [(mock-file "/src/rest_ful.clj") (mock-file "/src/rest_ful_2.clj")]
     :resource-files [(mock-file "/resources/index-site.html") (mock-file "/resources/some_crazy-File.js")]
     :test-source-files [(mock-file "/src/rest_ful_test.clj") (mock-file "/src/rest_ful_2_test.clj")]
     :test-resource-files [(mock-file "/resources/index-site.html") (mock-file "/resources/some_crazy-File.js")]})

(describe "File and namespace utils"

  (it "sanitizes a clojure namespace to file name"
    (should= (sanitize-from-clj "create-template") "create_template")
    (should= (sanitize-from-clj "create-template-more") "create_template_more")
    (should= (sanitize-from-clj "createless") "createless")
    (should= (sanitize-from-clj "create_less") "create_less"))

  (it "sanitizes a file name to clojure namespace"
    (should= (sanitize-to-clj "create_template") "create-template")
    (should= (sanitize-to-clj "create_template_more") "create-template-more")
    (should= (sanitize-to-clj "createless") "createless")
    (should= (sanitize-to-clj "create-less") "create-less"))

  (it "sanitizes a project name to lein-newnew template form"
    (should= (sanitize-project-name "leiningen.new.mytemplate" "mytemplate") "leiningen.new.{{sanitized}}")
    (should= (sanitize-project-name "leiningen.new.mytemplate.other" "mytemplate") "leiningen.new.{{sanitized}}.other")
    (should= (sanitize-project-name "mytemplate" "mytemplate") "{{sanitized}}"))

  (it "creates a relative file path for a file from an absolute root path"
    (should= (relative-path (jio/as-file "home/user/lein/template.clj") "home/user") "lein/template.clj")
    (should-not= (relative-path (jio/as-file "home/user/lein/template.clj") "home/use") "lein/template.clj"))

  (it "creates a relative path for files in the new lein template project"
    (should= (new-lein-path "home/user/mytemplates" "ringtemplate") "home/user/mytemplates/ringtemplate/src/leiningen/new/ringtemplate/"))
  )

(describe "Generating clj code files"

  (it "generates a new template project file"
    ()
    )


  )




(run-specs)