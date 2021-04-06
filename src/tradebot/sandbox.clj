(ns tradebot.sandbox
  (:require
    [clojure.core.async :as async]
    [manifold.stream :as ms]
    [tradebot.exchanges.coinbasepro.api :as coinbase.api]
    [tradebot.util :as util])
  (:import (java.util Date)))


;; Goals:
;; - Calculate average latency of ticker data
;; - Get trade stream for an asset
;; - Calculate orderbook imbalance for an asset at a time with best bid and ask
;; - Persist orderbook imbalance and trade data
;; - Calculate price change N trades after an orderbook imbalance

;; Ideas:
;; - Model "trade signal" streams as taps into a channel fed by the
;;   websocket feed

(def msg-xform
  "Parse JSON data from Coinbase Pro websocket feed, add received timestamp"
  (comp
    (map util/read-json-str)
    (map #(assoc % :received-timestamp (Date.)))))

(comment

  (defn subscribe!
    "Subscribes using sub-map and places messages onto to-chan.
    Returns a no-arg close-fn to close the websocket connection"
    [sub-map to-chan]
    (let [ws-conn @(coinbase.api/ws-conn-deferred)]
      (coinbase.api/ws-subscribe ws-conn sub-map)
      (ms/connect ws-conn to-chan)
      {:close-fn       #(.close ws-conn)
       :websocket-conn ws-conn
       :to-chan        to-chan}))

  (def subscription-map {:product-ids ["SKL-USD"],
                         :channels    ["ticker"]})

  (def subscribe-result
    (let [firehose-chan (async/chan 1 msg-xform)]
      (subscribe! subscription-map firehose-chan)))

  (async/<!! (:to-chan subscribe-result))

  (async/<!! (async/into [] (:to-chan subscribe-result)))

  ((:close-fn subscribe-result))

  nil)