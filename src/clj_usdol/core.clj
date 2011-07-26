(ns clj-usdol.core
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [clojure.string :as str_]
            [clj-usdol.oauth :as oauth]))

(oauth/hmac "s05xGSqZeSzqbTEw" "/V1/FORMS/Agencies?&Timestamp=2011-07-26T01:58:15Z&ApiKey=df34c7fa-ec1a-4ad5-b355-5ea9344d2f5e")
(def api-ver "V1")
(def usdol-url "http://api.dol.gov")
(declare token secret)

(defn auth-info [t, s]
  (def token t)
  (def secret s)
  )

(auth-info "" "") ;; You'll need to put your info here.

(defn- get-timestamp []
  (str_/replace
   (str (time/now))
   (re-find #"\.\d{3}" (str (time/now)))
   ""))

(defn- get-message [querystring]
  (oauth/hmac secret querystring))

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
                "?&" (get-querystring {:Timestamp (get-timestamp)
                                       :ApiKey token}))
        url (str (str_/join "/" [usdol-url, api-ver, dataset, table]) "?")
        auth (str "Timestamp=" (get-timestamp) "&ApiKey=" token "&Signature="
                  (get-message qs))]
    (client/get url
                {:headers {"Authorization" auth
                           "Accept" "application/json"}})))

(def agency-data (fetch-data "FORMS" "Agencies"))
