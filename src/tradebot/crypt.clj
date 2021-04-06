(ns tradebot.crypt
  (:import [javax.crypto Mac]
           [javax.crypto.spec SecretKeySpec]
           [java.util Base64]))

(defn base64-encode
  [^String s]
  (.encodeToString (Base64/getEncoder) (.getBytes s)))

(defn base64-decode
  [^String s]
  (.decode (Base64/getDecoder) s))

(defn hmac256
  "Calculate HMAC signature"
  [^String key ^String data]
  (let [algo "HmacSHA256"
        b64-encoder (Base64/getEncoder)
        signing-key (SecretKeySpec. (base64-decode key) algo)
        mac (doto (Mac/getInstance algo) (.init signing-key))]
    (.encodeToString b64-encoder (.doFinal mac (.getBytes data)))))