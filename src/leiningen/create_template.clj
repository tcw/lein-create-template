(ns leiningen.create-template
  (:use leiningen.classpath
        clojure.set
        stencil.core)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs])
  (import (java.io File FileNotFoundException)))

;------------------------------ Referential transparent---------------------------------------------

(def lein-new-relative-path "src/leiningen/new")

(def lein-new-sanitized "{{sanitized}}")

(defn sanitize-from-clj [file-name]
  (cs/replace file-name #"-" "_"))

(defn sanitize-to-clj [file-name]
  (cs/replace file-name #"_" "-"))

(defn sanitize-project-name [path project-name]
  (cs/replace path project-name lein-new-sanitized))

(defn relative-path [file root-path]
  (cs/replace (str file) (str root-path "/") ""))

(defn new-lein-path [root-path project-name]
  (str (cs/join "/" [root-path project-name lein-new-relative-path (sanitize-from-clj project-name)]) "/"))

(defn make-file-line [file root-path project-name clj?]
  (let [path (relative-path file root-path)
        sanitized-path (sanitize-project-name path project-name)
        sanitized-file-name (sanitize-from-clj (.getName file))]
    (str "[\"" sanitized-path "\" (render \"" sanitized-file-name "\"" (when clj? " data") ")]")))

(defn new-file-path [^File file new-path]
  (jio/as-file (str new-path (sanitize-from-clj (.getName file)))))

(defn get-new-sanitized-lein-file [file root-path new-project-name]
  (jio/as-file (str (new-lein-path root-path new-project-name) (sanitize-from-clj (.getName file)))))

(defn is-file-type [type file]
  (not (nil? (re-find (re-pattern (str "\\." type "$")) (str file)))))

(defn get-all-clj-files [info]
  (get (group-by
         #(or (is-file-type "clj" %) (is-file-type "cljs" %))
         (concat (:source-files info) (:test-source-files info) [(:project-file info)]))
    true))

(defn get-all-resource-files [info all-clj-files]
  (let [all-files (set (concat (:resource-files info)
                         (:test-resource-files info)
                         (:source-files info)
                         (:test-source-files info)))
        clj-files (set all-clj-files)]
    (difference all-files clj-files)))

;-----------------------------------------------------------------------------------------------------------

(defn walk [^File dir]
  (let [children (.listFiles dir)
        subdirs (filter #(.isDirectory %) children)
        files (filter #(.isFile %) children)]
    (concat files (mapcat walk subdirs))))

(defn get-files-recusivly [directory]
  (walk (jio/as-file directory)))

(defn copy-file [file root-path new-project-name]
  (let [new-file (get-new-sanitized-lein-file file root-path new-project-name)]
    (jio/make-parents new-file)
    (jio/copy file new-file)))

(defn copy-resource-files [files info]
  (doseq [file files]
    (copy-file file (:root-path info) (:new-project-name info))))

(defn copy-clj-file [file root-path old-project-name new-project-name]
  (let [new-file (get-new-sanitized-lein-file file root-path new-project-name)
        clj-text (slurp file)]
    (jio/make-parents new-file)
    (spit new-file (cs/replace clj-text old-project-name lein-new-sanitized))))

(defn copy-clj-files [files info]
  (doseq [file files]
    (copy-clj-file file (:root-path info) (:old-project-name info) (:new-project-name info))))

(defn create-template-render-file [clj-files resource-files info]
  (let [clj-lines (map #(make-file-line % (:root-path info) (:old-project-name info) true) clj-files)
        resource-lines (map #(make-file-line % (:root-path info) (:old-project-name info) false) resource-files)
        file-lines (concat clj-lines resource-lines)]
    (render-file "template" {:project (:new-project-name info) :files (apply str file-lines)})))

(defn create-project-template-file [info]
  (render-file "project" {:project (:new-project-name info)}))

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

(defn create-template
  [project & args]

  (if (empty? args)
    (println "You must enter a new template name!")

    (let [info (template-info project args)
          all-clj-files (get-all-clj-files info)
          all-resource-files (get-all-resource-files info all-clj-files)
          new-project-name (:new-project-name info)
          sanitized-new-project-name (sanitize-from-clj new-project-name)
          root-path (:root-path info)
          new-template-render-file (cs/join "/" [root-path new-project-name lein-new-relative-path (str sanitized-new-project-name ".clj")])
          new-project-file (cs/join "/" [root-path new-project-name "project.clj"])]

      (if (.exists (jio/as-file (str root-path "/" new-project-name)))
        (println "Can't create template, there already exists a folder named:" new-project-name)
        (do
          (copy-clj-files all-clj-files info)
          (copy-resource-files all-resource-files info)
          (spit new-template-render-file (create-template-render-file all-clj-files all-resource-files info))
          (spit new-project-file (create-project-template-file info)))))))