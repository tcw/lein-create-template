(ns leiningen.sanitize-utils
  (:use clojure.set)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs])
  (import (java.io File FileNotFoundException)))

(def lein-newnew-relative-path (str "src" (File/separator) "leiningen" (File/separator) "new"))

(def lein-newnew-sanitized "{{sanitized}}")

(def lein-newnew-sanitized-ns "{{ns-name}}")

(defn sanitize-from-clj [file-name]
  (cs/replace file-name #"-" "_"))

(defn relative-path [file root-path]
  (cs/replace (str file) (str root-path (File/separator)) ""))

(defn new-lein-path [root-path project-name]
  (str (cs/join (File/separator) [root-path project-name lein-newnew-relative-path
                                  (sanitize-from-clj project-name)]) (File/separator)))

(defn make-file-line [file root-path old-project-name clj?]
  (let [path (relative-path file root-path)
        sanitized-path (cs/replace path (sanitize-from-clj old-project-name) lein-newnew-sanitized)
        sanitized-file-name (sanitize-from-clj (.getName file))]
    (str "[\"" sanitized-path "\" (render \"" sanitized-file-name "\"" (when clj? " data") ")]\n")))

;TODO This can be improved
(defn sanitize-ns-in-clj-file [clj-text old-project-name]
  (cs/replace clj-text old-project-name lein-newnew-sanitized-ns))

(defn get-new-sanitized-lein-file [file root-path new-project-name]
  (jio/as-file (str (new-lein-path root-path new-project-name) (sanitize-from-clj (.getName file)))))

(defn get-new-template-render-file [root-path new-project-name]
  (cs/join (File/separator) [root-path new-project-name lein-newnew-relative-path (str (sanitize-from-clj new-project-name) ".clj")]))