(ns visitera.events
  (:require
   [ajax.core :as ajax]
   [ajax.edn :as ajax-edn]
   [re-frame.core :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [visitera.config :as cfg]))

;;dispatchers

(rf/reg-event-db
 :common/navigate
 (fn [db [_ match]]
   (let [old-match (:common/route db)
         new-match (assoc match :controllers
                          (rfc/apply-controllers (:controllers old-match) match))]
     (assoc db :common/route new-match))))

(rf/reg-fx
 :common/navigate-fx!
 (fn [[k & [params query]]]
   (rfe/push-state k params query)))

(rf/reg-event-fx
 :common/navigate!
 (fn [_ [_ url-key params query]]
   {:common/navigate-fx! [url-key params query]}))

(rf/reg-event-db
 :set-docs
 (fn [db [_ docs]]
   (assoc db :docs docs)))

(rf/reg-event-fx
 :fetch-docs
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             "/docs"
                 :response-format (ajax/raw-response-format)
                 :on-success       [:set-docs]}}))

(rf/reg-event-db
 :common/set-error
 (fn [db [_ error]]
   (assoc db :common/error error)))

(rf/reg-event-db
 :set-countries
 (fn [db [_ countries]]
   (assoc db :countries countries)))

(rf/reg-event-db
 :set-last-updated
 (fn [db [_ country]]
   (assoc db :last-updated country)))

(rf/reg-event-fx
 :page/init-home
 (fn [_ _]
   {:dispatch [:fetch-docs]}))

(rf/reg-event-fx
 :fetch-user-countries
 (fn [_ _]
   {:http-xhrio {:method :get
                 :uri "/api/user-countries"
                 :response-format (ajax-edn/edn-response-format)
                 :on-success [:set-countries]
                 :on-failure [:common/error]}}))

(rf/reg-event-fx
 :update-user-countries
 (fn [{:keys [db]} [_ country]]
   {:http-xhrio {:method :put
                 :uri "/api/user-countries"
                 :params country
                 :format (ajax-edn/edn-request-format)
                 :response-format (ajax-edn/edn-response-format)
                 :on-success [:set-countries]
                 :on-failure [:common/error]}
    :dispatch [:set-last-updated country]}))

;;subscriptions

(rf/reg-sub
 :common/route
 (fn [db _]
   (-> db :common/route)))

(rf/reg-sub
 :common/page-id
 :<- [:common/route]
 (fn [route _]
   (-> route :data :name)))

(rf/reg-sub
 :common/page
 :<- [:common/route]
 (fn [route _]
   (-> route :data :view)))

(rf/reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(rf/reg-sub
 :common/error
 (fn [db _]
   (:common/error db)))

(rf/reg-sub
 :countries
 (fn [db _]
   (:countries db)))

(defn- normalize-countries [countries]
  (into [] cat [(->> (:visited countries)
                     (map (fn [c-id] {:id c-id
                                      :fill (:visited cfg/colors)
                                      :status :visited})))
                (->> (:to-visit countries)
                     (map (fn [c-id] {:id c-id
                                      :fill (:to-visit cfg/colors)
                                      :status :to-visit})))]))

(rf/reg-sub
 :normalized-countries
 (fn []
   (rf/subscribe [:countries]))
 (fn [countries]
   (normalize-countries countries)))

(rf/reg-sub
 :visited-count
 (fn [db _]
   (-> db :countries :visited count)))

(rf/reg-sub
 :to-visit-count
 (fn [db _]
   (-> db :countries :to-visited count)))

(rf/reg-sub
 :last-updated
 (fn [db _]
   (:last-updated db)))
