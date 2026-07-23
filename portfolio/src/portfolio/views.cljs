(ns portfolio.views
  (:require
   [re-frame.core :as re-frame]
   [portfolio.subs :as subs]
   [portfolio.events :as events]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [markdown.core :refer [md->html]]
   ))

(defn posts-list []
  (let [posts @(re-frame/subscribe [::subs/post-slugs])]
    (conj [:div.blogbar] (map #(vector :button.cyber-btn {:key (:slug %) :on-click (fn [] (re-frame/dispatch [::events/fetch-post (:slug %)]))} (:slug %)) posts))))

(defn post-view []
  (let [cur-post @(re-frame/subscribe [::subs/cur-post])
        posts @(re-frame/subscribe [::subs/posts])]
    (if (some? cur-post)
      [:div {:dangerouslySetInnerHTML {:__html (md->html (get posts cur-post))}}])))


(defn render-math! [dom-node]
  (when (exists? js/renderMathInElement)
    (js/renderMathInElement 
     dom-node
     ;; Configure the delimiters you want to use in your markdown
     #js {:delimiters #js [#js {:left "$$" :right "$$" :display true}
                           #js {:left "$" :right "$" :display false}]
          :throwOnError false})))

(defn blog-post-view []
  (r/create-class
    {:component-did-mount
     (fn [this] 
       (render-math! (rdom/dom-node this)))
        
     :component-did-update
     (fn [this] 
       (render-math! (rdom/dom-node this)))
        
     :reagent-render
     (fn []
       (let [cur-post @(re-frame/subscribe [::subs/cur-post])
             posts    @(re-frame/subscribe [::subs/posts])]
         (if cur-post
           [:div {:dangerouslySetInnerHTML {:__html (md->html (get posts cur-post))}}]
           [:div "Loading post..."])))}))

(defn error-view []
  (let [error @(re-frame/subscribe [::subs/error])]
    [:p error]))

(defn title-view []
  [:div
   [:h2 "Hi, I'm Tim"]
   [:h3 "I'm a Robotics and AI undergraduate sharing all things engineering on this page!"]])

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        cur-post @(re-frame/subscribe [::subs/cur-post])]
    [:div
     [title-view]
     [posts-list]
     [:h1 cur-post]
     [blog-post-view]]))
