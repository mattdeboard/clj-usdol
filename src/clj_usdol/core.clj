(ns clj-usdol.core
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [clojure.string :as str_]
            [clj-usdol.oauth :as oauth]
            )
  (:require clojure.contrib.json))

;; Set some "global" vars.
(def api-ver "V1")
(def usdol-url "http://api.dol.gov")
;; token and secret will be used later.
(declare token secret)

;; Sets values for API token and shared secret
(defn auth-info [t, s]
  (def token t)
  (def secret s)
  )

;; Returns a Java-style UTC timestamp, but with the milliseconds (e.g. ".750"
;; stripped by clojure.string/replace.
(defn- get-timestamp []
  (str_/replace
   (str (time/now))
   (re-find #"\.\d{3}" (str (time/now)))
   ""))

;; Returns HMAC SHA1 hex digest. The API docs do not specify hex, so it took a
;; bit of trial and error to find the right scheme.
(defn- get-message [querystring]
  (oauth/hmac secret querystring))

;; querystring is a slight misnomer, as this actually returns an auth string,
;; but thanks to the unclear explanations and convoluted authentication scheme
;; the Dept. of Labor chose to use for their API, 'querystring' is... as un-
;; confusing a choice as can be made, I think.
(defn- get-querystring [vals]
  (apply str 
         (for [i vals]
           (if (= i (first vals))
             (str (name (key i)) "=" (val i))
             (str "&" (name (key i)) "=" (val i))))))

(defn fetch-data [dataset, table]
  "This function 'reaches out' to the remote server and touches it with the
   header as described in the API's standard."
  (let [qs (str "/" (apply str (interpose "/" [api-ver, dataset, table]))
                "?&" (get-querystring {:Timestamp (get-timestamp)
                                       :ApiKey token}))
        url (str (str_/join "/" [usdol-url, api-ver, dataset, table]) "?")
        auth (str "Timestamp=" (get-timestamp) "&ApiKey=" token "&Signature="
                  (get-message qs))]
    (client/get url
                {:headers {"Authorization" auth
                           "Accept" "application/json"}})))

;; You'll need to put your info here.
(auth-info "df34c7fa-ec1a-4ad5-b355-5ea9344d2f5e" "s05xGSqZeSzqbTEw")
;; agency-data will contain the json data. To do: Add flag to choose either XML
;; or json format.  The API supports either. Except for $metadata, of course.
(def agency-data (fetch-data "FORMS" "Agencies"))
