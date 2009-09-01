(ns bfc.jena.test-suite
  (:use [clojure.test :only (*load-tests* run-tests)]))

(def test-names [:node :ontology])

(def test-namespaces
  (map #(symbol (str "bfc.jena." (name %))) test-names))

(defn run []
  (binding [*load-tests* true]
    (apply require :reload-all test-namespaces)
    (apply run-tests test-namespaces)))
