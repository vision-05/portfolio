(ns portfolio.events
  (:require
   [re-frame.core :as re-frame]
   [portfolio.db :as db]
   [ajax.core :as ajax]
   [ajax.edn :as ajax-edn]
   [day8.re-frame.http-fx :as http-fx]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
  ::fetch-post-slugs
  (fn [{:keys [db]} _]
    {:db db
     :http-xhrio {:method :get
                  :uri "/posts.edn"
                  :response-format (ajax-edn/edn-response-format)
                  :on-success [::slugs-fetch-success]
                  :on-failure [::slugs-fetch-failure]}}))

(re-frame/reg-event-fx
 ::fetch-post
 (fn [{:keys [db]} [_ post-slug]]
   {:db db
    :http-xhrio {:method :get
                 :uri (str "/posts/" post-slug ".md")
                 :response-format (ajax/raw-response-format)
                 :on-success [::post-fetch-success post-slug]
                 :on-failure [::post-fetch-failure]}}))

(re-frame/reg-event-db
 ::slugs-fetch-success
 (fn [db [_ post-slugs]]
   (assoc db :post-slugs post-slugs)))

(re-frame/reg-event-db
 ::slugs-fetch-failure
 (fn [db _]
   (assoc db :error "Failed to fetch post index!")))

(re-frame/reg-event-db
 ::post-fetch-success
 (fn [db [_ post-slug markdown]]
   (assoc-in (assoc db :cur-post post-slug) [:posts post-slug] markdown)))

(re-frame/reg-event-db
 ::post-fetch-failure
 (fn [db _]
   (assoc db :error "Failed to fetch post")))