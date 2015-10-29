(ns avs_v2.handler
  (:require [compojure.core :refer [defroutes]]
            [avs_v2.routes.home :refer [home-routes]]
            [avs_v2.routes.auth :refer [auth-routes]]
            [avs_v2.middleware :refer [load-middleware]]
            [noir.response :refer [redirect]]
            [noir.util.middleware :as noir-middleware]
            [noir.session :as session]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [avs_v2.db.schema :as schema]
            [ring.middleware.format :refer [wrap-restful-format]]
            [selmer.parser :refer [add-tag!]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/appender-fn})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "avs_v2.log" :max-size (* 512 1024) :backlog 10})

  (if (env :dev) (parser/cache-off!))

  ;; Initialize the database -- TODO: check if exists
  ;;(schema/create-all-tables)
  ;; New CSRF tag
  (add-tag! :csrf-token (fn [_ _] (anti-forgery-field)))
  (timbre/info "avs_v2 started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "avs_v2 is shutting down..."))

  (defn app-page [_]
    (session/get :usernm))

(def app (noir-middleware/app-handler
           ;; add your application routes here
           [auth-routes home-routes app-routes]
           ;; add custom middleware here
           :middleware [wrap-restful-format]
           :middleware [wrap-anti-forgery]
           ;; timeout sessions after 5 minutes
           :session-options {:timeout (* 60 5)
                             :timeout-response (redirect "/")}
           ;; add access rules here
           :access-rules [{:redirect "/login"
                :rule app-page}]
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:json-kw :edn]))
