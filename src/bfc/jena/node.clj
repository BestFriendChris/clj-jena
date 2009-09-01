(ns bfc.jena.node
  (:use clojure.test)
  (:require bfc.inline-tester)
  (:import (com.hp.hpl.jena.datatypes.xsd XSDDatatype)))

(def *current-ontologies* {})

(with-test
(defn resource [& tripples]
  {::type ::resource
   :tripples (apply hash-map tripples)})

  (testing "resource"
    (is (= (resource :a 75)
           {::type ::resource
            :tripples {:a 75}}))))

(with-test
(defn named-resource [id & tripples]
  (assoc (apply resource tripples) :id id))

  (testing "named-resource"
    (is (= (named-resource "foo"
                           :a 75)
           {:id "foo"
            ::type ::resource
            :tripples {:a 75}}))))

(defstruct literal ::type :literal-type :value)
(def integer-literal (partial struct literal ::literal XSDDatatype/XSDinteger))
(def string-literal (partial struct literal ::literal XSDDatatype/XSDstring))

(declare save!)

  (with-test
  (defn- ontology-bind [t]
    (if (keyword? t) (*current-ontologies* t) t))

    (testing "ontology-bind"
      (binding [*current-ontologies* {:a 1}]
        (is (= (ontology-bind 3) 3))
        (is (= (ontology-bind :a) 1)))))

  (defmulti convert-predicate (fn [_ p] (::type p)))
  (defmethod convert-predicate ::literal  [model p] (.createTypedLiteral model (:value p) (:literal-type p)))
  (defmethod convert-predicate ::resource [model p] (save! model p))
  (defmethod convert-predicate  :default  [model p] (ontology-bind p))

  (defn- fix-tripples [model props]
    (map (fn [m]
           (let [obj (key m)
                 pred (val m)]
             [(ontology-bind obj) (convert-predicate model pred)]))
         props))

  (defn- create-resource [model id]
    (if id
      (.createResource model id)
      (.createResource model)))

(defn save!
  ([model ontologies resource] (binding [*current-ontologies* (apply merge ontologies)]
                                 (save! model resource)))
  ([model resource]
    (let [r (create-resource model (:id resource))
          tripples (fix-tripples model (:tripples resource))]
      (dorun (map (fn [[obj pred]] (.addProperty r obj pred)) tripples))
      r)))

(when *load-tests* (import '(com.hp.hpl.jena.rdf.model ModelFactory)))
(when *load-tests* (require '[bfc.jena.ontology :as ont]))
(when *load-tests*

  (def URI  "http://bfc/testing.owl#")
  (def PREFIX "whatever")

  (ont/defontology ontology URI
    :whatever/node (ont/resource "Node")
    :whatever/name (ont/property "name")
    :whatever/link (ont/property "link"))

  (let [m (ModelFactory/createDefaultModel)]
    (.setNsPrefix m PREFIX URI)
    (save! m [ontology]
           (named-resource (str URI "id")
                           :is-a :whatever/node
                           :whatever/link (resource :is-a :whatever/node
                                                    :whatever/name (string-literal "foo"))))
    ;(.write m *out* "RDF/XML-ABBREV")
    )
  )

