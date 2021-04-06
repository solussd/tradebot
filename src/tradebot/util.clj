(ns tradebot.util
  (:require
    [clojure.data.json :as json]
    [clojure.string :as str]))

(defn clj->json-key-fn
  [k]
  (str/replace (name k) \- \_))

(defn json->clj-key-fn
  [k]
  (keyword (str/replace k \_ \-)))

(defn json-str
  [m]
  (json/write-str m :key-fn clj->json-key-fn))

(defn build-url
  "Basic URL builder"
  ([base-url path querystring-params]
   (let [qs (cond-> ""
              (seq querystring-params)
              (str \? (str/join \&
                                (map (fn [[k v]]
                                       (str (name k) \= v))
                                     querystring-params))))]
     (format "%s/%s%s" base-url path qs)))
  ([base-url path]
   (build-url base-url path nil)))

(defn read-json-str
  [s]
  (json/read-str s :key-fn json->clj-key-fn))

(defn ns-prefix-kw
  [kw]
  (keyword (str *ns* \. (namespace kw)) (name kw)))
