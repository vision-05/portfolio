(ns portfolio.db)

(def default-db
  {:name "re-frame"
   :post-slugs {}
   :cur-post nil
   :posts {}
   :error "No Error"
   :loading-posts? false})
