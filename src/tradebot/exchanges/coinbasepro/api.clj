(ns tradebot.exchanges.coinbasepro.api
  (:require
    [aleph.http :as http]
    [manifold.deferred :as d]
    [manifold.stream :as ms]
    [byte-streams :as bs]
    [clojure.core.async :as async :refer [go go-loop <!! >!! <! >!]]
    [clojure.string :as str]
    [clojure.data.json :as json]
    [tradebot.util :as util]
    [tradebot.crypt :as crypt]
    [manifold.deferred :as d])
  (:import [java.util Date]))

(def ^:const API_URL "https://api.pro.coinbase.com")
(def ^:const WS_FEED_URL "wss://ws-feed.pro.coinbase.com")

(defn ws-conn-deferred
  "Returns a deferred which yields a connection to the Coinbase Pro
   websocket feed"
  []
  (http/websocket-client WS_FEED_URL {:max-frame-payload 2048000}))

(defn sig-data
  ([payload]
   (let [timestamp        (int (/ (.getTime (Date.)) 1000))
         key              (System/getenv "CB_API_KEY")
         secret           (System/getenv "CB_API_SECRET")
         passphrase       (System/getenv "CB_API_PASSPHRASE")
         payload-json-str (if payload (util/json-str payload) "")
         message-str      (format "%sGET/users/self/verify%s"
                                  timestamp payload-json-str)
         signature        (crypt/hmac256 secret message-str)]
     {:key        key
      :signature  signature
      :passphrase passphrase
      :timestamp  timestamp}))
  ([]
   (sig-data nil)))

(defn auth-headers
  [payload]
  (let [sd (sig-data payload)]
    {:CB-ACCESS-KEY        (:key sd)
     :CB-ACCESS-SIGN       (:signature sd)
     :CB-ACCESS-TIMESTAMP  (:timestamp sd)
     :CB-ACCESS-PASSPHRASE (:passphrase sd)}))

(defn ws-subscribe
  "Subscribe to specific topics on the websocket feed"
  [ws-conn subscription-map]
  (let [subscribe-payload (-> subscription-map
                              (assoc :type "subscribe")
                              (merge (sig-data))
                              util/json-str)]
    @(ms/put! ws-conn subscribe-payload)))

(defn orderbook-snapshot
  "Returns a deferred which yields an orderbook for the given
  product-id and level."
  [product-id level]
  (let [url (util/build-url API_URL
                            (format "products/%s/book" product-id)
                            {:level level})]
    (d/chain' (http/get url {:headers {:User-Agent "bc1"}})
              :body
              bs/to-string
              util/read-json-str)))

(comment

  ()


  nil)

