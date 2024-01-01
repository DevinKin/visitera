(ns visitera.validation
  (:require
   [struct.core :as st]))

(def register-schema
  [[:email
    st/required
    st/string
    st/email]

   [:password
    st/required
    st/string
    {:message "password must contain at least 8 characters"
     :validate #(> (count %) 7)}]])

(defn validate-register [params]
  (first (st/validate params register-schema)))

(def login-schema
  [[:email
    st/required
    st/string
    st/email]

   [:password
    st/required
    st/string]])

(defn validate-login [params]
  (first (st/validate params login-schema)))
