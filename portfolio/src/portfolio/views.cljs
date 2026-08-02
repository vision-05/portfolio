(ns portfolio.views
  (:require
   [re-frame.core :as re-frame]
   [portfolio.subs :as subs]
   [portfolio.events :as events]
   [reagent.core :as r]
   [reagent.dom.client :as rdomc]
   [clojure.string :as str]
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
          :throwOnError true})))


(defn expand-attrs [s]
  (str/replace s #"(?m)^(#{1,6})\s+(.*?)\s*\{\.([\w-]+)\}\s*$"
               (fn [[_ hashes text cls]]
                 (let [n (count hashes)]
                   (str "<h" n " class=\"" cls "\">" text "</h" n ">")))))

(defn csv->md-table [rows]
  (let [esc #(str/replace (str %) "|" "\\|")
        fmt #(str "| " (str/join " | " (map esc %)) " |")
        [header & body] rows]
    (str/join "\n"
      (concat [(fmt header)
               (fmt (repeat (count header) "---"))]
              (map fmt body)))))

(defn blog-post-view []
  (let [!el (atom nil)
        ref-fn #(reset! !el %)]
    (r/create-class
      {:component-did-mount
       (fn [_]
         (when-let [el @!el]
           (render-math! el)))
       :component-did-update
       (fn [_]
         (when-let [el @!el]
           (render-math! el)))
       :reagent-render
       (fn []
         (let [cur-post @(re-frame/subscribe [::subs/cur-post])
               posts @(re-frame/subscribe [::subs/posts])]
           (if (some? cur-post)
             [:div.post {:ref ref-fn
                    :dangerouslySetInnerHTML (r/unsafe-html (md->html (expand-attrs (get posts cur-post))))}]
             [:div "Loading post..."])))})))

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
