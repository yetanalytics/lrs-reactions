(ns com.yetanalytics.lrs-reactions.spec
  (:require [clojure.spec.alpha :as s #?@(:cljs [:include-macros true])]
            [xapi-schema.spec :as xs]
            [com.yetanalytics.lrs-reactions.path :as path]))

(s/def ::condition-name
  string?)

(s/def ::path
  (s/every
   (s/or :string string?
         :index nat-int?)
   :gen-max 4))

(s/def ::val (s/or :string string?
                   :number number?
                   :null nil?
                   :boolean boolean?))

(s/def :ref/condition ::condition-name)

(s/def ::ref
  (s/keys :req-un [:ref/condition
                   ::path]))

(s/def ::op
  #{"gt"
    "lt"
    "gte"
    "lte"
    "eq"
    "noteq"
    "like"
    "contains"})

(defn valid-like-val?
  [{:keys [op
           ref
           ;; note that val is conformed here
           val]}]
  (if (= op "like")
    (if-let [{ref-path :path} ref]
      (let [{:keys [leaf-type]} (path/analyze-path ref-path)]
        (= 'string leaf-type))
      (= :string
         (first val)))
    true))

(defn valid-clause-path?
  [{:keys [path
           op
           val
           ref] :as clause}]
  (if path
    (let [{:keys [valid?
                  leaf-type
                  next-keys]} (path/analyze-path path)]
      (and valid?
           (if (= "contains" op)
             (or (= '[idx] next-keys) (= 'json leaf-type))
             (and leaf-type
                  (or
                   (= 'json leaf-type) ;; anything goes
                   (if-let [{ref-path :path} ref]
                     (let [{ref-leaf-type :leaf-type}
                           (path/analyze-path ref-path)]
                       (= leaf-type ref-leaf-type))
                     (= (name leaf-type)
                        (name (first val)))))))))
    true))

(s/def ::condition
  (s/and
   (s/keys :req-un
           [(or
             (and ::path ::op
                  (or ::val ::ref))
             (or ::and ::or ::not))])
   valid-like-val?
   valid-clause-path?))

(s/def ::and (s/every ::condition
                      :min-count 1
                      :gen-max 3))
(s/def ::or (s/every ::condition
                     :min-count 1
                     :gen-max 3))
(s/def ::not ::condition)

(s/def ::conditions
  (s/map-of simple-keyword?
            ::condition
            :min-count 1
            :gen-max 3))

(defn valid-identity-path?
  [path]
  (some? (:leaf-type (path/analyze-path path))))

(s/def ::identityPaths
  (s/every (s/and ::path
                  valid-identity-path?)))

;; A JSON structure resembling a statement, but with path refs to cond results
(s/def ::template ::xs/any-json)

(s/def ::ruleset
  (s/keys :req-un [::identityPaths
                   ::conditions
                   ::template]))

(s/def :reaction.error/type
  #{"ReactionQueryError"
    "ReactionTemplateError"
    "ReactionInvalidStatementError"})

(s/def :reaction.error/message string?)

(s/def ::error
  (s/nilable
   (s/keys :req-un [:reaction.error/type
                    :reaction.error/message])))
