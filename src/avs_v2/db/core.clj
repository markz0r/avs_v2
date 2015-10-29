(ns avs_v2.db.core
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [avs_v2.db.schema :as schema]
            [clojure.string :as string]))

(defdb db schema/db-spec)

;; ############################################
;; DEPARTMENT RECORDS
;; ############################################
(defentity departments
  entity-fields :id :name)

(defn create-department [d_name]
   (insert departments
          (values {:name d_name
                   :created (sqlfn now)})))

;; ############################################
;; USER RECORDS
;; ############################################
(defentity employees)
(defn create-employee [email fname lname role department created]
   (insert employees
          (values {:email (string/lower-case email)
                   :first_name (string/lower-case fname)
                   :last_name (string/lower-case lname)
                   :role (string/lower-case role)
                   :department department
                   :created (sqlfn now)
                   :current true})))

(defn get-emp-id [fname lname]
  (first (select employees
          (fields :id)
  (where (and (like :first_name fname)
              (like :last_name lname))))))

(defentity avs_users)
(defn get-avs-user [username]
  (first (select avs_users
          (where {:username username
                  :active true})
          (limit 1))))

;; ############################################
;; APPLICATION RECORDS
;; ############################################
  (defentity bt_applications)
  (defn create-application [name responsible created ext_access]
     (insert bt_applications
          (values {:name (string/lower-case name)
                   :dept_responsible responsible
                   :active true
                   :external_access ext_access})))

  (defn get-app-id [app-name]
    (first (select bt_applications
          (fields :id)
  (where (or    {:tablename (string/lower-case app-name)}
                (or    {:name (string/lower-case app-name)}
                       {:filename (string/lower-case app-name)}))))))

  (defn get-app-name-by-id [app_id] (first (select bt_applications (fields :name)
                                      (where {:id (. Integer parseInt app_id)}))))

 (defn get-app-name [app_ref] (first (select bt_applications (fields :name)
                              (where (or   {:filename (string/lower-case app_ref)}
                                           {:tablename (string/lower-case app_ref)})))))

  (defn get-app-table [app_id]
    ((first (select bt_applications
          (fields :tablename)
      (where {:id (. Integer parseInt app_id)}))) :tablename))

  (defn get-active-apps []
    (select bt_applications
          (where {:active true})))

  (defn update-import-date [app_id]
    (update bt_applications
            (set-fields {:last_import (sqlfn now)})
            (where {:id (. Integer parseInt app_id)})))

;; ############################################
;; ENTRY RECORDS
;; ############################################
  (defentity unix)
  (defentity active_directory)
  (defentity rt)
  (defentity biolock_user)
  (defentity biolock_device)
  (defentity jde)
  (defentity network)
  (defentity jira)
  (defentity au_portal)
  (defentity recreg)
  (defentity titan)

(defn create-entry [app_table app_id emp_id username email detail status & [comments]]
  (try
    (insert app_table
      (values {:timestamp (sqlfn now)
            :app_id app_id
            :emp_id emp_id
            :username (string/lower-case username)
            :email (string/lower-case email)
            :detail detail
            :status status
            :comments comments}))
  (catch Exception ex (.getMessage ex))))

(defn update-entry-status [tablename id new_status]
  (update tablename
          (set-fields {:status new_status
                       :action_time (sqlfn now)})
          (where {:id id})))

(defn update-bulk-status [tablename current_status new_status]
  (update tablename
          (set-fields {:status new_status
                       :action_time (sqlfn now)})
          (where {:status current_status})))

(defn get-entry-by-id [tablename status id]
  (first(select tablename
          (where (and {:id id}
                      {:status status})))))

(defn delete-entry [entry-type entry-id]
  (delete entry-type
          (where {:id (. Integer parseInt entry-id)})))

(defn update-comments [app_id entry_id comments]
  (update (get-app-table app_id)
          (set-fields {:comments comments})
          (where {:id (. Integer parseInt entry_id)})))

(defn get-entries [tablename status]
  (def app_name ((get-app-name tablename) :name))
  (defn assoc-name [row] (assoc row :app_name app_name))
  (map assoc-name (select tablename
                      (where (and {:timestamp [>= (raw "now() - INTERVAL '1 year'")]}
                      {:status status})))))


(defn get-fuzzy-entries-app [in_vector]
  (defn assoc-name [row] (assoc row :app_name (in_vector 3)))
  (map assoc-name (select (in_vector 0)
                  (where(and   (= :status (. Integer parseInt (in_vector 1)))
                        (or   (like :username (string/lower-case (str \%(in_vector 2)\%)))
                              (like :email (string/lower-case (str \%(in_vector 2)\%)))))))))


(defn search-all-tables [status phrase]
   (defn new_vect [row] (vector (row :tablename) status phrase (row :name)))
   (flatten (map get-fuzzy-entries-app (map new_vect (get-active-apps)))))

;; ############################################
;; DONE
;; ############################################
