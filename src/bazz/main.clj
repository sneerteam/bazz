(ns bazz.main
  (:gen-class)

  (:require [bazz.core :as core]
            [bazz.templates :as templates]
            [bazz.clones :as clones]
            [org.httpkit.server :as http-kit :refer [run-server]]
            [compojure.core :refer [defroutes context GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.core.async :as async]
            [clojure.java.browse :refer [browse-url]]
            [clojure.data.json :as json]))

(def state (atom nil))

(defn show-page [peer-products]
  (require 'bazz.templates :reload)
  {:headers {"Content-Type" "text/html;charset=utf8"}
   :body (templates/home (core/product-list) peer-products)})

(defn show-home []
  (show-page []))

(defn show-products [peer-login]
  (show-page (core/peer-product-list peer-login)))


(defn run-peer-product [peer product req]
  (http-kit/with-channel req http-channel
    (http-kit/send! http-channel (str "Cloning " product ": ") false)
    (let [response-channel (async/chan 10)]
      (http-kit/on-close http-channel (fn [_] (async/close! response-channel)))
      (clones/serve-clone-request peer product (core/peer-product-path peer product) response-channel)
      (async/go
       (loop []
         (when-let [response (async/<! response-channel)]
           (http-kit/send! http-channel response false) ; false means dont close
           (recur)))
       (http-kit/close http-channel)))))

(defn choose-encoding-for [{{accept "accept"} :headers}]
  (if (re-find #"\bapplication/edn\b" (or accept ""))
    ["application/edn" pr-str]
    ["application/json" json/write-str]))

(defn my-products [req]
  (let [[content-type encoder] (choose-encoding-for req)]
    {:headers {"Content-Type" content-type
               "Access-Control-Allow-Origin" "*"} ;; required to let clients execute directly from the file system
     :body (encoder (core/product-list))}))

(defroutes web-app
  (GET "/" [] (show-home))
  (GET "/products" [peer] (show-products peer))
  (GET "/products/:peer/:product/run"
       [peer product]
       (partial run-peer-product peer product))
  (context "/api" []
    (GET "/my-products" [] my-products))
  (route/resources "/public" "public"))

(defn start-server [port]
  (let [app (-> #'web-app handler/site)]
    {:port port
     :server-closer (run-server app {:port port})}))

(defn start []
  (let [port 8080]
    (swap! state #(do
      (assert (nil? %))
      (start-server port)))))

(defn stop []
  (if-let [{:keys [server-closer cloning-process] :as old} @state]
    (try
      (server-closer)
      (finally
        (swap! state #(if-not (identical? % old) % nil))))))

;(start)
;(stop)
;http://localhost:8080/

(defn -main [& args]
  (let [state (start)]
    (browse-url (str "http://localhost:" (:port state)))))
