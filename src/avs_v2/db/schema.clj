(ns avs_v2.db.schema
(:require [clojure.java.jdbc :as sql]))

; define database connection
(def db-spec
  {:subprotocol "postgresql"
   :subname "//localhost/avs_v2"
   :user "avs"
   :password ""})

(defn create-department-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :departments
      [:id "SERIAL PRIMARY KEY"]
      [:name "varchar(50)"]
      [:created :timestamp]
     )))

(defn create-employee-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :employees
      [:id "SERIAL PRIMARY KEY"]
      [:email "varchar(100)"]
      [:first_name "varchar(50)"]
      [:last_name "varchar(50)"]
      [:role "varchar(50)"]
      [:department "INTEGER references departments(id)"]
      [:created :timestamp]
      [:current :boolean]
     )))


(defn create-app-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :bt_applications
      [:id "SERIAL PRIMARY KEY"]
      [:name "varchar(100)"]
      [:filename "varchar(100)"]
      [:dept_responsible "INTEGER references departments"]
      [:last_import :timestamp]
      [:created :timestamp]
      [:active :boolean]
      [:external_access :boolean])))

(defn create-unreviewed-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :unreviewed
      [:id "SERIAL PRIMARY KEY"]
      [:timestamp :timestamp]
      [:app_id "INTEGER references bt_applications(id)"]
      [:emp_id "INTEGER references employees(id)"]
      [:username "varchar(100)"]
      [:email "varchar(100)"]
      [:detail "varchar(200)"]
      [:comments "text"])))

(defn create-accepted-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :accepted
      [:id "SERIAL PRIMARY KEY"]
      [:timestamp :timestamp]
      [:app_id "INTEGER references bt_applications(id)"]
      [:emp_id "INTEGER references employees(id)"]
      [:username "varchar(100)"]
      [:email "varchar(100)"]
      [:detail "varchar(200)"]
      [:actor "INTEGER references employees(id)"]
      [:action_time :timestamp]
      [:comments "text"])))

(defn create-rejected-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :rejected
      [:id "SERIAL PRIMARY KEY"]
      [:timestamp :timestamp]
      [:app_id "INTEGER references bt_applications(id)"]
      [:emp_id "INTEGER references employees(id)"]
      [:username "varchar(100)"]
      [:email "varchar(100)"]
      [:actor "INTEGER references employees(id)"]
      [:action_time :timestamp]
      [:detail "varchar(200)"]
      [:resolved :boolean]
      [:resolve_comment :text]
      [:resolver "INTEGER references employees(id)"]
      [:resolve_time :timestamp]
      [:comments "text"])))


(defn create-avsusers-table []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :avs_users
      [:timestamp :timestamp]
      [:emp_id "INTEGER references employees(id)"]
      [:username "varchar(100)"]
      [:email "varchar(100)"]
      [:active :boolean]
     )))

(defn drop-table
  "drops the supplied table from the DB, table name must be a keyword
eg: (drop-table :users)"
  [table]
  (try
   (sql/db-do-commands db-spec
    (sql/drop-table-ddl table))
   (catch Exception _)))

(defn create-all-tables
  "creates the database tables used by the application"
  []
  (create-department-table)
  (create-employee-table)
  (create-app-table)
  (create-unreviewed-table)
  (create-accepted-table)
  (create-rejected-table)
  (create-avsusers-table))

(defn drop-all-tables
  "creates the database tables used by the application"
  []
  (drop-table :avs_users)
  (drop-table :unreviewed)
  (drop-table :accepted)
  (drop-table :rejected)
  (drop-table :employees)
  (drop-table :bt_applications)
  (drop-table :departments))

(defn reset-db []
  (drop-all-tables)
  (create-all-tables))
