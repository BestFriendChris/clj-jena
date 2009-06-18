(ns bfc.jena.ontology
  (:use clojure.contrib.test-is)
  (:require bfc.inline-tester)
  (:import [com.hp.hpl.jena.rdf.model ResourceFactory]
           [com.hp.hpl.jena.vocabulary RDF]))

(declare *URI*)

(defmacro defontology [name uri & resources]
  `(binding [*URI* ~uri]
     (def ~name (hash-map :is-a com.hp.hpl.jena.vocabulary.RDF/type ~@resources))))

(defn resource [id] (ResourceFactory/createResource (str *URI* id)))
(defn property [id] (ResourceFactory/createProperty (str *URI* id)))

(when *load-tests*
  (defontology testing-ontology "http://bfc/testing.owl#"
               :a (resource "a")
               :b (property "b"))

  (deftest testing-property
    (is (= (testing-ontology :is-a) RDF/type))
    (are (= (.getURI (testing-ontology _1)) _2)
         :a "http://bfc/testing.owl#a"
         :b "http://bfc/testing.owl#b")))

(comment
;; Usage:
(ns bfc.whatever.ontology
  (:use bfc.jena.ontology))

(def URI "http://bestfriendchris.com/ontologies/2009/05/23/whatever.owl#")
(def PREFIX "whatever")

(defontology ontology URI
  :whatever/node (resource "Node")
  :whatever/name (property "name")
  :whatever/link (property "link"))

)

