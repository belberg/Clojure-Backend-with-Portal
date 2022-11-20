(ns my-backend.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as ring-defaults]
            [compojure.core :as c]
            [compojure.route :as route]
            [hiccup2.core :as h]
            [muuntaja.core :as m]
            [muuntaja.middleware :as muuntaja]
            [portal.api :as p]))

(defonce server (atom nil))

(defn home-view [tel]
  [:html
   [:body
    [:h1 "Welcome home!"]
    [:ul
     (for [i (range tel)]
       [:li i])]]])

(def p (p/open)) ; open portal viewer in browser
(add-tap #'p/submit) ; Add portal as a tap> target

(defn routes []
  (c/routes
   (c/GET "/" [tel]
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (str (h/html (home-view (Integer. (or tel 0)))))})
   (c/GET "/:foo" [foo]
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (str "you asked for " foo)})
   (c/POST "/api" [:as req]
           (clojure.pprint/pprint (:body-params req))
           (tap> req)
           {:status 200
            :body {:hello 123}})))

(defn handler [req]
  ((routes) req))

(defn start-jetty! []
  (reset!
   server
   (jetty/run-jetty (-> #'handler
                        muuntaja/wrap-format
                        (ring-defaults/wrap-defaults
                         ring-defaults/api-defaults))
                    {:join? false
                     :port 3428})))

(defn stop-jetty []
  (.stop @server)
  (reset! server nil))

(defn -main [& args]
  (start-jetty!))

                                        ;(start-jetty!)
