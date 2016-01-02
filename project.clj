(defproject lein-create-template "0.1.2"
  :description "A Leiningen plugin for creating templates from existing skeleton projects"
  :url "http://github.com/tcw/lein-create-template"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[speclj "2.1.2"]
                 [org.clojure/clojure "1.7.0"]
                 [stencil "0.3.0"]]
  :plugins [[speclj "2.1.2"]]
  :test-paths ["spec/"]
  :resource-paths ["resources/" "dev_resources/"]
  :eval-in-leiningen true)