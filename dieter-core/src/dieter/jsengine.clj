(ns dieter.jsengine
  (:require [dieter.settings :as settings]
            [dieter.pools :as pools]
            [dieter.rhino :as rhino]
            [dieter.v8 :as v8]
            [clojure.string :as string]))

(def asset-path-pattern
  (re-pattern (str "("
                   (string/join "|" (settings/asset-roots))
                    ")/\\./assets(.*\\.\\.)/")))

(defn strip-filename [filename]
  (string/replace
    filename
    asset-path-pattern
    ""))

;; TODO: take an asset to avoid slurping here
(defn run-compiler [pool preloads fn-name file & {:keys [engine]}]
  (let [input (slurp file)
        absolute (.getAbsolutePath file)
        filename (strip-filename (.getPath file))
        args [input absolute filename]]
    (if (or (= (:engine settings/*settings*) :rhino)
            (= engine :rhino))
      (rhino/with-scope pool preloads
        (rhino/call fn-name args))
      (v8/with-scope pool preloads
        (v8/call fn-name args)))))
