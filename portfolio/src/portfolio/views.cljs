(ns portfolio.views
  (:require
   [re-frame.core :as re-frame]
   [portfolio.subs :as subs]
   [portfolio.events :as events]
   [markdown.core :refer [md->html]]
   ))

(defn posts-list []
  (let [posts @(re-frame/subscribe [::subs/post-slugs])]
    (conj [:ul] (map #(vector :button {:key (:slug %) :on-click (fn [] (re-frame/dispatch [::events/fetch-post (:slug %)]))} (:slug %)) posts))))

(defn post-view []
  (let [cur-post @(re-frame/subscribe [::subs/cur-post])
        posts @(re-frame/subscribe [::subs/posts])]
    (if (some? cur-post)
      [:div {:dangerouslySetInnerHTML {:__html (md->html (get posts cur-post))}}])))

(defn error-view []
  (let [error @(re-frame/subscribe [::subs/error])]
    [:p error]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        cur-post @(re-frame/subscribe [::subs/cur-post])]
    [:div
     [error-view]
     [posts-list]
     [:h1 cur-post]
     [post-view]]))
