(ns leiningen.file-utils
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs])
  (import (java.io File FileNotFoundException)))

(defn delete-file-recursively
  [f & [silently]]
  (let [f (jio/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (jio/delete-file f silently)))

(def tmp-dir (System/getProperty "java.io.tmpdir"))

;done the google guava way
(defn create-tmp-dir [tmp-dir-name attempts]
  (let [millis (System/currentTimeMillis)]
    (loop [attempts-done 1]
      (if (= attempts attempts-done)
        nil
        (let [dir (jio/as-file (str tmp-dir tmp-dir-name millis attempts-done))]
          (if (.mkdir dir)
            dir
            (recur (+ 1 attempts-done))))))))

(defn walk [^File dir]
  (let [children (.listFiles dir)
        subdirs (filter #(.isDirectory %) children)
        files (filter #(.isFile %) children)]
    (concat files (mapcat walk subdirs))))

(defn walk-dirs [directories]
  (set (mapcat #(walk (jio/as-file %)) directories)))

(defn copy-file-force-path[source destination]
  (jio/make-parents destination)
  (jio/copy source destination))

(defn create-project-template [project-name]
  (str "(defproject " project-name "/lein-template \"0.1.0-SNAPSHOT\"\n  :description \"Created with lein-create-template\"\n  :url \"http://example.com/FIXME\"\n  :license {:name \"Eclipse Public License\"\n            :url \"http://www.eclipse.org/legal/epl-v10.html\"}\n  :eval-in-leiningen true)"))

(defn create-newnew-template [project-name files]
  (str "(ns leiningen.new." project-name "\n  (:use [leiningen.new.templates :only [renderer name-to-path sanitize-ns ->files]]))\n\n(def render (renderer \"" project-name "\"))\n\n(defn " project-name "\n  [name]\n  (let [data {:name name\n              :ns-name (sanitize-ns name)\n              :sanitized (name-to-path name)}]\n    (->files data " files ")))"))