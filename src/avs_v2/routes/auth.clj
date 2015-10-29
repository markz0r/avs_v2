(ns avs_v2.routes.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [clj-ldap.client :as client]
            [compojure.core :refer :all]
            [avs_v2.routes.home :refer :all]
            [avs_v2.layout :as layout]
            [noir.session :as session]
            [noir.response :as response]
            [avs_v2.util :as util]
            [avs_v2.db.core :as db]))

  (defn authenticate [username password & [attributes]]
    (let [server (client/connect util/host)
          qualified-name (str username "@" (-> "blah.local"))]
      (if (client/bind? server qualified-name  password)
        (first (client/search server
          "OU=Staff,DC=ausregistrygroup,DC=local"
           {:filter (str "sAMAccountName=" username)
           :attributes (or attributes [])})))))

  (defn login-page [&[error]]
    (layout/render "login.html" {:error error}))

  (defn session-create [username password]
    (if (get (db/get-avs-user username) :active)
      (if (authenticate username password)
        ((session/put! :usernm username)(session/put! :avs-userid 1)))))

  (defn handle-login [username password]
    (session-create username password)
    (if (session/get :usernm) (response/redirect "/")
      (login-page "Authentication failed")))

  (defn handle-logout []
      (session/clear!)
      (login-page "Your session has been destroyed"))


  (defroutes auth-routes
    (GET "/login" []
         (login-page))
    (POST "/login" [username password]
          (handle-login username password))
    (GET "/logout" []
         (handle-logout)))
