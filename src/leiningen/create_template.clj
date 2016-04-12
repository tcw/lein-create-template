(ns leiningen.create-template
  (:use clojure.set)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs]
            [leiningen.file-utils :as fu]
            [leiningen.sanitize-utils :as su])
  (import (java.io File FileNotFoundException)))

(defn get-all-clj-files [info]
  (set (get (group-by
              #(or (fu/is-file-type "clj" %) (fu/is-file-type "cljs" %))
              (concat
                (:source-files info)
                (:test-files info)
                (:template-additions info)
                [(:project-file info)]))
         true)))

(defn get-all-files [info]
  (set (concat (:resource-files info)
         (:source-files info)
         (:test-files info)
         (:java-files info)
         (:template-additions info)
         [(:project-file info)])))

(defn copy-file [file root-path new-project-name]
  (let [new-file (su/get-new-sanitized-lein-file file root-path new-project-name)]
    (fu/copy-file-force-path file new-file)))

(defn copy-resource-files [files info]
  (doseq [file files]
    (copy-file file (:root-path info) (:new-project-name info))))

(defn copy-clj-file [file root-path old-project-name new-project-name]
  (let [new-file (su/get-new-sanitized-lein-file file root-path new-project-name)
        clj-text (slurp file)]
    (jio/make-parents new-file)
    (spit new-file (su/sanitize-ns-in-clj-file clj-text old-project-name))))

(defn copy-clj-files [files info]
  (doseq [file files]
    (copy-clj-file file (:root-path info) (:old-project-name info) (:new-project-name info))))

(defn create-template-render-file [clj-files resource-files info]
  (let [clj-lines (map #(su/make-file-line % (:root-path info) (:old-project-name info) true) clj-files)
        resource-lines (map #(su/make-file-line % (:root-path info) (:old-project-name info) false) resource-files)
        file-lines (concat clj-lines resource-lines)]
    (fu/create-newnew-template (:new-project-name info) (apply str file-lines))))

(defn template-info [project args]
  (let [root-path (:root project)]
    {:root-path root-path
     :old-project-name (:name project)
     :new-project-name (first args)
     :project-file (jio/as-file (str root-path (File/separator) "project.clj"))
     :source-files (fu/get-files-recusivly (:source-paths project))
     :template-additions (for [f (:template-additions project)] (jio/as-file f))
     :resource-files (fu/get-files-recusivly (:resource-paths project))
     :java-files (fu/get-files-recusivly (:java-source-paths project))
     :test-files (fu/get-files-recusivly (:test-paths project))}))

(defn create-template
  [project & args]
  (if (empty? args)
    (println "You must enter a new template name!\nusage: lein create-template <template-name>")

    (let [info (template-info project args)
          all-clj-files (get-all-clj-files info)
          all-resource-files (difference (get-all-files info) all-clj-files)
          new-project-name (:new-project-name info)
          root-path (:root-path info)
          new-template-render-file (su/get-new-template-render-file root-path new-project-name)
          new-project-file (cs/join (File/separator) [root-path new-project-name "project.clj"])]

      (if (.exists (jio/as-file (str root-path (File/separator) new-project-name)))
        (println "Can't create template, there already exists a folder named:" new-project-name)
        (do
          (copy-clj-files all-clj-files info)
          (copy-resource-files all-resource-files info)
          (spit new-template-render-file (create-template-render-file all-clj-files all-resource-files info))
          (spit new-project-file (fu/create-project-template (:new-project-name info)))
          (println (str "Created template project:" new-project-name)))))))
