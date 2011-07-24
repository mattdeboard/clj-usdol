(ns clj-usdol
  (:require [clj-http.client :as client]
            digest
            [clj-time.core :as time]
            [clojure.string :as str_]))

;;(digest/sha1 (str "foo" "bar"))
(def api-ver "V1")
(def usdol-url "http://api.dol.gov")
(declare token secret)

(defn auth-info [t, s]
  (def token t)
  (def secret s)
  )

(auth-info "df34c7fa-ec1a-4ad5-b355-5ea9344d2f5e" "s05xGSqZeSzqbTEw")

(defn- get-timestamp []
  (str_/replace
   (str (time/now))
   (re-find #"\.\d{3}" (str (time/now)))
   ""))

(defn- get-message [querystring]
  (digest/sha1
   (str querystring secret)))

(defn- get-querystring [vals]
  (apply str 
         (for [i vals]
           (if (= i (first vals))
             (str (name (key i)) "=" (val i))
             (str "&" (name (key i)) "=" (val i))))))

(get-querystring {:Timestamp (get-timestamp)
                  :ApiKey token})
(defn fetch-data [dataset, table]
  (let [qs (str "/" (apply str (interpose "/" [api-ver, dataset, table]))
                "&" (get-querystring {:Timestamp (get-timestamp)
                                      :ApiKey token}))]
    (prn qs)
    (println (str "Timestamp=" (get-timestamp) "&ApiKey="
                  token "&Signature=" (get-message qs)))
    (client/get (apply str (interpose "/" [usdol-url, api-ver, dataset, table]))
                {:headers
                 {"Authorization" (str "Timestamp=" (get-timestamp) "&ApiKey="
                                       token "&Signature=" (get-message qs))
                  }})))
(apply str (interpose "/" [api-ver, "FORMS", "Agencies"]))
(fetch-data "FORMS" "Agencies")

(apply str (interpose ":" ["a" "b" "c"]))
