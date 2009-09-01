(ns bfc.jena.node
  (:use clojure.test)
  (:require bfc.inline-tester)
  (:import (com.hp.hpl.jena.datatypes.xsd XSDDatatype)))

(defn resource [ontologies & tripples]
  (let [merged-ontology (apply merge ontologies)]
    {:tripples (map (fn [t] (if (keyword? t) (merged-ontology t) t)) tripples)}))

(defn named-resource [id ontologies & tripples]
  (assoc (apply resource ontologies tripples) :id id))

(defstruct type-literal :value :type)
(defn integer-literal [v] (struct type-literal v XSDDatatype/XSDinteger))
(defn string-literal  [v] (struct type-literal v XSDDatatype/XSDstring))

(declare save!)

  (defn- create-typed-literal [model type-literal]
    (.createTypedLiteral model (:value type-literal) (:type type-literal)))

  (defn- fix-predicate [model p]
    (cond (map? p) ; struct type-literal
            (.createTypedLiteral model (:value p) (:type p))
          (seq? p) ; resource
            (save! model p)
          :else p))

  (defn- map2
    "Applies f to sets of two items in a single coll. If coll is not even,
    last item is ignored.  Function f should accept 2 arguments."
    [f coll] (map f (take-nth 2 coll) (take-nth 2 (rest coll))))

  (defn- fix-tripples [model & props]
    (map2 (fn [obj pred] [obj (fix-predicate model pred)]) props))

  (defn- create-resource [model id]
    (if id
      (.createResource model id)
      (.createResource model)))

(defn save! [model resource]
  (let [r (create-resource model (:id resource))]
    (dorun (map (fn [[obj pred]] (.addProperty r obj pred)) (fix-tripples (:tripples resource))))
    r))

(comment
;; Example usage:
(ns bfc.whatever
  (:require [bfc.whatever.ontology :as whatever]
            [bfc.jena.node :as jena]))

(defn link-resource [{:keys [name]}]
  (jena/resource [whatever/ontology]
                 :is-a :whatever/node
                 :whatever/name (jena/string-literal name)))

(defn whatever-resource [{:keys [id name link]}]
  (jena/named-resource id [whatever/ontology]
                 :is-a :whatever/node
                 :whatever/name (jena/string-literal name)
                 :whatever/link (link-resource link)))

(jena/save! model
  (whatever-resource {:name "foo" :link {:name "bar"}}))
)

