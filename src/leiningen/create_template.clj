(ns leiningen.create-template
  (:use leiningen.classpath
        clojure.set)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs])
  (import (java.io File FileNotFoundException)))


(def lein-project-template
  "(defproject ##projectname##/lein-template \"0.1.0-SNAPSHOT\"
  :description \"FIXME: write description\"
  :url \"http://example.com/FIXME\"
  :license {:name \"Eclipse Public License\"
            :url \"http://www.eclipse.org/legal/epl-v10.html\"}
  :eval-in-leiningen true)")

(def lein-new-template
  "(ns leiningen.new.##projectname##
  (:use [leiningen.new.templates :only [renderer name-to-path ->files]]))

(def render (renderer \"##projectname##\"))

(defn ##projectname##
  \"FIXME: write documentation\"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
               ##filelines##
             )))")


(def lein-new-relative-path "src/leiningen/new")

(def lein-new-sanitized "{{sanitized}}")


; TEXT UTILS

(defn new-lein-path [root-path project-name]
  (str (cs/join "/" [root-path project-name lein-new-relative-path project-name]) "/"))

(defn sanitize-from-clj [file-name]
  (cs/replace file-name #"-" "_"))

(defn sanitize-to-clj [file-name]
  (cs/replace file-name #"_" "-"))

(defn sanitize-project-name [path project-name]
  (cs/replace path project-name lein-new-sanitized))

(defn relative-path [file root-path]
  (cs/replace (str file) (str root-path "/") ""))

(defn make-file-line [file root-path project-name clj?]
  (let [path (relative-path file root-path)
        sanitized-path (sanitize-project-name path project-name)
        sanitized-file-name (sanitize-to-clj (.getName file))]
    (str "[\"" sanitized-path "\" (render \"" sanitized-file-name "\"" (when clj? " data") ")]")))

(defn add-to-template [template replace-tag text]
  (cs/replace
    template
    replace-tag
    (apply str text)))

(defn templify-ns [clj-text old-project-name]
  (let [old-name (str old-project-name ".")
        new-name (str lein-new-sanitized ".")]
    (add-to-template clj-text old-name new-name)))

; FILE UTILS

(defn is-file-type [type file]
  (not (nil? (re-find (re-pattern (str "\\." type "$")) (str file)))))

(defn new-file-path [^File file new-path]
  (jio/as-file (str new-path (.getName file))))

(defn walk [^File dir]
  (let [children (.listFiles dir)
        subdirs (filter #(.isDirectory %) children)
        files (filter #(.isFile %) children)]
    (concat files (mapcat walk subdirs))))

(defn get-files-recusivly [directory]
  (walk (jio/as-file directory)))

(defn copy-file [file root-path new-project-name]
  (let [file-path (new-lein-path root-path (sanitize-from-clj new-project-name))]
    (let [file-new-path (new-file-path file file-path)]
      (jio/make-parents file-new-path)
      (jio/copy file file-new-path))))

(defn copy-clj-file [clj-file root-path old-project-name new-project-name]
  (let [file-path (new-lein-path root-path (sanitize-from-clj new-project-name))]
    (let [new-file (new-file-path clj-file file-path)
          clj-text (slurp clj-file)]
      (jio/make-parents new-file)
      (spit new-file (templify-ns clj-text old-project-name)))))

(defn template-info [project args]
  (let [root-path (:root project)]
    {:root-path root-path
     :old-project-name (last (cs/split root-path #"/"))
     :new-project-name (first args)
     :project-file (jio/as-file (str root-path "/project.clj"))
     :source-files (get-files-recusivly (str root-path "/src"))
     :resource-files (get-files-recusivly (str root-path "/resources"))
     :test-source-files (get-files-recusivly (str root-path "/test"))
     :test-resource-files (get-files-recusivly (str root-path "/test-resources"))}))

(defn get-all-clj-files [info]
  (get (group-by
         #(or (is-file-type "clj" %) (is-file-type "cljs" %))
         (concat (:source-files info) (:test-source-files info)))
    true))

(defn get-all-resource-files [info all-clj-files]
  (let [all-files (into #{} (concat (:resource-files info)
                              (:test-resource-files info)
                              (:source-files info)
                              (:test-source-files info)))
        clj-files (into #{} all-clj-files)]
    (difference all-files clj-files)))

(defn create-template-lines [info resource-files clj-files]
  (let [clj-lines (map #(make-file-line % (:root-path info) (:old-project-name info) true) clj-files)
        resource-lines (map #(make-file-line % (:root-path info) (:old-project-name info) false) resource-files)]
    (concat clj-lines resource-lines)))

(defn copy-resource-files [files info]
  (doseq [file files]
    (copy-file file (:root-path info) (:new-project-name info))))

(defn copy-clj-files [files info]
  (doseq [file files]
    (copy-clj-file file (:root-path info) (:old-project-name info) (:new-project-name info))))

(defn create-template
  [project & args]
  (let [info (template-info project args)
        all-clj-files (get-all-clj-files info)
        all-resource-files (get-all-resource-files info all-clj-files)]

    (copy-clj-files all-clj-files info)
    (copy-resource-files all-resource-files info)

        (let [project-name (sanitize-from-clj (:new-project-name info))
              file-name (str project-name ".clj")
              file (cs/join "/" [(:root-path info) project-name lein-new-relative-path file-name])
              lines (cs/join "\n\t\t" (create-template-lines info all-clj-files all-resource-files))
              template-part (add-to-template lein-new-template "##filelines##" lines)
              template (add-to-template template-part "##projectname##" (sanitize-to-clj project-name))]
          (spit file template))

        (let [project-name (sanitize-from-clj (:new-project-name info))
              new-project-file (cs/join "/" [(:root-path info) project-name "project.clj"])
              project-text (add-to-template lein-project-template "##projectname##" (sanitize-to-clj project-name))]
          (spit new-project-file project-text))

    ))


