(ns visitera.handler-test
  (:require
   [clojure.test :refer :all]
   [mount.core :as mount]
   [muuntaja.core :as m]
   [ring.mock.request :refer :all]
   [visitera.handler :refer :all]
   [visitera.middleware.formats :as formats]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'visitera.config/env
                 #'visitera.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 302 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))
