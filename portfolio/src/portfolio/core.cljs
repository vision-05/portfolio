(ns portfolio.core
  (:require
   [reagent.dom.client :as rdomc]
   [re-frame.core :as re-frame]
   [portfolio.events :as events]
   [portfolio.views :as views]
   [portfolio.config :as config]
   ))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defonce !root (atom nil))

(defn root []
  (or @!root
      (reset! !root (rdomc/create-root (.getElementById js/document "app")))))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdomc/render (root) [views/main-panel]))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/fetch-post-slugs])
  (dev-setup)
  (mount-root))
