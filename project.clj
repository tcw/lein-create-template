(defproject lein-create-template "0.1.0"
  :description "A Leiningen plugin for creating templates from existing skeleton projects"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[speclj "2.1.2"]
                 [org.clojure/clojure "1.4.0"]
                 [stencil "0.3.0"]]
  :plugins [[speclj "2.1.2"]]
  :test-paths ["spec/"]
  :resource-paths ["resources/"]
  :eval-in-leiningen true)