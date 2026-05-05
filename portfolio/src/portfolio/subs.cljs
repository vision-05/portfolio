(ns portfolio.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::post-slugs
 (fn [db]
   (:post-slugs db)))

(re-frame/reg-sub
 ::posts
 (fn [db]
   (:posts db)))

(re-frame/reg-sub
 ::cur-post
 (fn [db]
   (:cur-post db)))

(re-frame/reg-sub
  ::error
  (fn [db]
    (:error db)))