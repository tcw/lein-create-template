(ns leiningen.create-template
  (:use leiningen.classpath)
  (:require [clojure.java.io :as jio])
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

(defn mytemp
  \"FIXME: write documentation\"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
             ##sourcefiles##
             ##resourcefiles##
             )))")

;[\"src/{{sanitized}}/foo.clj\" (render \"foo.clj\" data)]

(defn create-relative-sanitized-file-path [file root-path project-name]
  (let [relative-path (clojure.string/replace (str file) root-path "")]
    (clojure.string/replace relative-path project-name "{{sanitized}}")))

(defn sanitize-file-name [file-name]
  (clojure.string/replace file-name #"-" "_"))

(defn replace-project-name [template project-name]
  (clojure.string/replace template #"##projectname##" project-name))

(defn add-resource-files-to-new-template [new-template root-path project-name resource-files]
  (clojure.string/replace
    new-template
    #"##resourcefiles##"
    (apply str (map
      #(str "\"" (create-relative-sanitized-file-path % root-path project-name) "\" (render \"" (sanitize-file-name (.getName %)) "\")\n")
      resource-files))))

(defn add-source-files-to-new-template [new-template root-path project-name source-files]
  (clojure.string/replace
    new-template
    #"##sourcefiles##"
    (apply str (map
      #(str "\"" (create-relative-sanitized-file-path % root-path project-name) "\" (render \"" (sanitize-file-name (.getName %)) "\" data)\n")
      source-files))))

(defn create-lein-new-file [new-template root-path project-name new-project-name resource-files source-files]
  (let [template-with-resources (add-resource-files-to-new-template new-template root-path project-name resource-files)
    source-and-resources (add-source-files-to-new-template template-with-resources root-path project-name source-files)]
    (replace-project-name source-and-resources new-project-name)
    ))



(defn new-path [^File file new-path]
  (jio/as-file (str new-path (.getName file))))

(defn walk [^File dir]
  (let [children (.listFiles dir)
        subdirs (filter #(.isDirectory %) children)
        files (filter #(.isFile %) children)]
    (concat files (mapcat walk subdirs))))

(defn get-files-recusivly [directory]
  (walk (jio/as-file directory)))

(defn copy-resources [resource-files root-path project-name]
  (let [new-file-path (str root-path "/" project-name "/src/leiningen/new/" project-name "/")]
    (doseq [resource resource-files]
      (let [file (new-path resource new-file-path)]
        (jio/make-parents file)
        (jio/copy resource file)))))

(defn create-template-file [text root-path project-name file-name]
  (spit (str root-path "/" project-name "/" file-name) text))

(defn copy-clj-files [clj-files root-path project-name]
  ;TODO
  )

(defn create-template
  [project & args]
  (let [root-path (:root project)
        project-name (last (clojure.string/split root-path #"/"))
        new-project-name (first args)
        project-file (jio/as-file (str root-path "/project.clj"))
        source-files (get-files-recusivly (str root-path "/src"))
        resource-files (get-files-recusivly (str root-path "/resources"))
        test-files (get-files-recusivly (str root-path "/test"))
        test-resource-files (get-files-recusivly (str root-path "/test-resources"))]

    (copy-resources resource-files root-path new-project-name)
    (copy-resources test-resource-files root-path new-project-name)
    (create-template-file
      (replace-project-name lein-project-template new-project-name)
      root-path
      new-project-name
      "project.clj")
    (println (str "resources : " (apply str (map #(str %) resource-files))))
    (println (str "sources : " (apply str (map #(str %) source-files))))
    (create-template-file
      (create-lein-new-file lein-new-template root-path project-name new-project-name resource-files source-files)
      root-path
      (str new-project-name "/src/leiningen/new")
      (str (sanitize-file-name new-project-name) ".clj")
      )))


