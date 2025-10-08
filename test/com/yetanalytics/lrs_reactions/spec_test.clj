(ns com.yetanalytics.lrs-reactions.spec-test
  (:require [clojure.test :refer [deftest are testing]]
            [com.yetanalytics.lrs-reactions.spec :as rs]
            [xapi-schema.spec :as xs]))

(deftest valid-like-val-test
  (testing "validates value in case of like op"
    (are [clause result]
        (= result (true? (rs/valid-like-val? clause)))
      {:path ["id"]
       :op "like"
       :val [:string "foo"]} true
      ;; invalid val type
      {:path ["id"]
       :op "like"
       :val [:number 1]} false
      ;; invalid ref path type
      {:path ["id"]
       :op "like"
       :ref {:condition "whatever"
             :path ["result" "score" "scaled"]}} false)))

(deftest valid-like-val-2-test
  (testing "validates value in case of like op (xAPI 2.0.0)"
    (are [clause result]
         (binding [xs/*xapi-version* "2.0.0"]
           (= result (true? (rs/valid-like-val? clause))))
      {:path ["context" "contextAgents" 0 "relevantTypes" 0]
       :op "like"
       :val [:string "https://example.org/friend"]} true
      ;; invalid val type
      {:path ["context" "contextAgents" 0 "relevantTypes" 0]
       :op "like"
       :val [:number 1]} false
      ;; invalid ref path type
      {:path ["context" "contextAgents" 0 "relevantTypes" 0]
       :op "like"
       :ref {:condition "whatever"
             :path ["result" "score" "scaled"]}} false)))

(deftest valid-clause-path-test
  (testing "validates logic clause paths"
    (are [clause result]
        (= result (true? (rs/valid-clause-path? clause)))
      {:path ["id"],
       :op "eq",
       :val [:string "bar"]} true
      {:path ["context" "extensions" "https://example.com/array"],
       :op "contains",
       :val [:string "bar"]} true
      ;; incomplete path
      {:path ["object"],
       :op "eq",
       :val [:string "bar"]} false
      ;; invalid path
      {:path ["foo"],
       :op "eq",
       :val [:string "bar"]} false
      ;; type mismatch
      {:path ["result" "score" "scaled"]
       :op "eq"
       :val [:string "bar"]} false
      ;; same but for ref
      {:path ["result" "score" "scaled"]
       :op "eq"
       :ref {:condition "whatever"
             :path ["id"]}} false)))

(deftest valid-clause-path-2-test
  (testing "validates logic clause paths (xAPI 2.0.0)"
    (are [clause result]
         (binding [xs/*xapi-version* "2.0.0"]
           (= result (true? (rs/valid-clause-path? clause))))
      {:path ["context" "contextAgents" 0 "agent" "name"],
       :op "eq",
       :val [:string "Bob"]} true
      ;; incomplete path
      {:path ["context" "contextAgents" 0 "agent"],
       :op "eq",
       :val [:string "bob"]} false)))

(deftest valid-identity-path-test
  (testing "validates identity paths"
    (are [path result]
        (= result (rs/valid-identity-path? path))
      ["id"] true
      ;; incomplete
      ["object"] false
      ;; not xapi
      ["foo"] false)))

(deftest valid-identity-path-2-test
  (testing "validates identity paths"
    (are [path result]
         (binding [xs/*xapi-version* "2.0.0"]
           (= result (rs/valid-identity-path? path)))
      ["context" "contextAgents" 0 "agent" "mbox"] true
      ;; incomplete
      ["context" "contextAgents" 0 "agent"] false)))
