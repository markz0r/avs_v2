(ns avs_v2.routes.home
  (:use compojure.core)
  (:require [avs_v2.layout :as layout]
            [avs_v2.util :as util]
            [avs_v2.db.core :as db]
            [noir.util.route :refer :all]
            [noir.session :as session]
            [noir.response :as response]))

(def ent_vec (vector "unreviewed", "accepted", "rejected", "resolved"))
(defn process-action [app_id action id]
  (try
    (case action
      "accept"       (db/update-entry-status (db/get-app-table app_id)  id 1)
      "reject"       (db/update-entry-status (db/get-app-table app_id)  id 2)
      "resolve"      (db/update-entry-status (db/get-app-table app_id)  id 3)
      "unaccept"     (db/update-entry-status (db/get-app-table app_id)  id 2)
      "accept_bulk"  (db/update-bulk-status  (db/get-app-table app_id)  id 1)
      "reject_bulk"  (db/update-bulk-status  (db/get-app-table app_id)  id 2)
      "resolve_bulk" (db/update-bulk-status  (db/get-app-table app_id)  id 3))
    "0"
  (catch Exception ex (.getMessage ex))))


(defn process-comment [app_id entry_id comments] (response/json
                                                (try (db/update-comments app_id entry_id comments) "0"
                                                  (catch Exception ex (.getMessage ex)))))

(defn entry-action [app_id action id]
  (response/json (process-action app_id action (. Integer parseInt id))))

(defn home-page []
  (layout/render
    "home.html" {:AppList (db/get-active-apps)}))

(defn search-page []
  (layout/render "search.html"))

(defn list-entries [app_id status]
  (layout/render
    "list.html" {:status   status
                 (keyword (str (ent_vec (. Integer parseInt status)) "Entries")) (db/get-entries (db/get-app-table app_id) (. Integer parseInt status))
                 :appList (db/get-active-apps)
                 :entry_type (ent_vec (. Integer parseInt status))
                 :selected_app_id app_id
                 :selected_app_name ((db/get-app-name-by-id app_id) :name)}))


(defn run-job [app_id &[username password]]
 (response/json
  (try
    (util/exec-job app_id username password)
    (db/update-import-date app_id)
    "Submitted successfully"
   (catch Exception ex (.getMessage ex)))))

(defn broad-search [phrase]
  (layout/render
    "search.html" { :submitted true
                    :unreviewedEntries  (db/search-all-tables "0" phrase)
                    :acceptedEntries    (db/search-all-tables "1" phrase)
                    :rejectedEntries    (db/search-all-tables "2" phrase)
                    :resolvedEntries    (db/search-all-tables "3" phrase)}))

(defn jobs-page []
  (layout/render "jobs.html" {:AppList (util/get-applist)}))

(defn test-page []
  (layout/render "test.html"))

(def-restricted-routes home-routes
  (GET "/" [] (home-page))
  (GET "/search" [] (search-page))
  (GET "/list" [] (list-entries "1" "0"))
  (POST "/list" [app_id status] (list-entries app_id status))
  (GET "/jobs" [] (jobs-page))
  (POST "/jobs" [app_id username password] (run-job app_id username password))
  (POST "/jobs" [app_id] (run-job app_id))
  (POST "/entry-action" [app_id action id] (entry-action app_id action id))
  (POST "/entry-comment" [app_id entry_id comments] (process-comment app_id entry_id comments))
  (POST "/search" [phrase] (broad-search phrase))
  (GET "/no-javascript" [] (test-page)))
