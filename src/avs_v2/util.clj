(ns avs_v2.util
  (:require [noir.io :as io]
            [markdown.core :as md]
            [avs_v2.db.core :as db]
            [clj-ldap.client :as client]
            [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (->>
    (io/slurp-resource filename)
    (md/md-to-html-string)))

(def host
  {:host
    [{;:address "melad01"
      :address "melad01"
      :port 389
      :connect-timeout (* 1000 5)
      :timeout (* 1000 30)
      :ssl? true}]})

(def UserAccountControl_Flags (vector
"SCRIPT"
"ACCOUNTDISABLE"
"UNKNOWN"
"HOMEDIR_REQUIRED"
"LOCKOUT"
"PASSWD_NOTREQD"
"PASSWD_CANT_CHANGE"
"ENCRYPTED_TEXT_PWD_ALLOWED"
"TEMP_DUPLICATE_ACCOUNT"
"NORMAL_ACCOUNT"
"UNKNOWN"
"INTERDOMAIN_TRUST_ACCOUNT"
"WORKSTATION_TRUST_ACCOUNT"
"SERVER_TRUST_ACCOUNT"
"UNKNOWN"
"UNKNOWN"
"DONT_EXPIRE_PASSWORD" ;65536
"MNS_LOGON_ACCOUNT"
"SMARTCARD_REQUIRED"
"TRUSTED_FOR_DELEGATION"
"NOT_DELEGATED"
"USE_DES_KEY_ONLY"
"DONT_REQ_PREAUTH"
"PASSWORD_EXPIRED"
"TRUSTED_TO_AUTH_FOR_DELEGATION"
"UNKNOWN"
"PARTIAL_SECRETS_ACCOUNT"))
;; ############################################
;; IMPORT FILE FRESHNESS
;; ############################################
  (def text_file_path "/var/lib/avs/")
  ;(def text_file_path "/home/markc/Dropbox/WORK/avs_v2/incomming_data/")
  (defn mod-date [filename] (.lastModified (java.io.File. (str text_file_path filename))))
  (defn append-filemod[row] (assoc row :file_mod (mod-date (row :filename))))
  (defn get-applist []  (map append-filemod (db/get-active-apps)))

;; ############################################
;; GENERIC IMPORTING FUNCTIONS
;; ############################################
  (defn insert-entry [tablename appid username email detail status & [comments]]
    (db/create-entry tablename appid 1 username email detail status comments))

  (defn openfile [filename]
    (with-open [rdr (clojure.java.io/reader (str text_file_path filename))] (reduce conj [] (line-seq rdr))))

  (defn splitter [entry delimeter retVal] (get (str/split entry delimeter) retVal))

;; ############################################
;; IMPORTING UNIX USERS
;; ############################################
  (def all_unix_users (openfile "unix-users-txt"))
  (def unix_id ((db/get-app-id "unix-users-txt") :id))
  (defn unix_name_regex [line] (vector ((str/split line #",") 2) ((str/split line #",") 3)))
                                      ;group id  (str (split_unix 4) ", " (split_unix 5))))
  (def unique_unix_users (set (map unix_name_regex all_unix_users)))

  (defn groups-mapper [line] {:host (line 1) :group (line 2) :members
                                    (if (< (count line) 5) "NA" (line 4))})
  (def all_unix_groups (map groups-mapper (map #(str/split %1 #",")(openfile "unix-groups-txt"))))

  (defn get-user-detail [username] (group-by :group
                                             (map #(dissoc %1 :members) (filter #(.contains (%1 :members) username) all_unix_groups))))

 (defn unix-detail-string [host_group] (map #(get %1 :host) host_group))
 (defn unix-detailer [filt_ugh]
   (str (filt_ugh 0) ": "
        (clojure.string/join ", " (flatten (map unix-detail-string (rest filt_ugh))))))

  (defn insert_unixuser_hardcoded [entry]
    (def user_detail (get-user-detail (entry 0)))
    (insert-entry "unix" unix_id (entry 0) (str/join ", " (keys user_detail)) (entry 1) 0
                  (str/join "\n \n" (map unix-detailer user_detail))))

  (defn run_unixuser_import [] (doall (map insert_unixuser_hardcoded  unique_unix_users)))

;; ############################################
;; IMPORTING USER/MACHINE/GROUP
;; ############################################
;; Need a new table for all user-machine-group-combinations (perhaps generate and delete on fly)


;; ############################################
;; IMPORTING RT Users
;; ############################################
  (def all_rt  (openfile "rt-data-txt"))
  (def rt_id ((db/get-app-id "rt-data-txt") :id))
  (defn insert_rt_hardcoded [entry]
    (insert-entry "rt" rt_id (splitter entry #":" 1) (splitter entry #":" 0)  (splitter entry #":" 2) 0))

  (defn run_rt_import [] (doall (map insert_rt_hardcoded all_rt)))

;; ############################################
;; IMPORTING BIO LOCK USERS
;; ############################################
  (def all_bio (openfile "biolock-users-txt"))
  (def biouser_id ((db/get-app-id "biolock-users-txt") :id))
  (defn insert_biolocks_hardcoded [entry]
    (insert-entry "biolock_user" biouser_id (splitter entry #"[;:]" 1)
                       (if (zero? (. Integer parseInt (splitter entry #";" 1))) "INACTIVE" "ACTIVE")
                       (splitter entry #";" 2) 0))

  (defn run_biolock_import [] (doall (map insert_biolocks_hardcoded all_bio)))

;; ############################################
;; IMPORTING BIO LOCK DEVICES
;; ############################################
  (def all_bio_dev (openfile "biolock-units-txt"))
  (def biodev_id ((db/get-app-id "biolock-units-txt") :id))
  (defn insert_biounit_hardcoded [entry]
    (insert-entry "biolock_device" biodev_id
                       (splitter entry #"[:]" 1) "NA" (splitter entry #"[:]" 0) 0))

  (defn run_biounit_import [] (doall (map insert_biounit_hardcoded all_bio_dev)))

;; ############################################
;; IMPORTING JDE Users
;; ############################################
  (def all_jde (openfile "jde-data-txt"))
  (def jde_id ((db/get-app-id "jde-data-txt") :id))

  (defn insert_jde_hardcoded [entry]
    (insert-entry "jde" jde_id
    (str/trim (splitter entry #":" 1)) (str/trim (splitter entry #":" 0)) (str/trim (splitter entry #":" 2)) 0))

  (defn run_jde_import [] (doall (map insert_jde_hardcoded all_jde)))

;; ############################################
;; IMPORTING Network Users
;; ############################################
  (def all_network (openfile "network-users-txt"))
  (def network_id ((db/get-app-id "network-users-txt") :id))
  (defn network_name_regex [line] (get (str/split line #",") 1))

  (def unique_network_users (set (map network_name_regex all_network)))

  (defn insert_network_hardcoded [entry] (insert-entry "network" network_id entry "unknown" "NA" 0))

  (defn run_network_import [] (doall (map insert_network_hardcoded  unique_network_users)))

;; ############################################
;; IMPORTING JIRA Users
;; ############################################
  (def all_jira (openfile "jira-users-txt"))
  (def jira_id ((db/get-app-id "jira-users-txt") :id))
  (defn insert_jira_hardcoded [entry]
  (insert-entry "jira" jira_id (splitter entry #":" 1) (splitter entry #":" 2) (splitter entry #":" 4) 0))

  (defn run_jira_import [] (doall (map insert_jira_hardcoded all_jira)))

;; ############################################
;; IMPORTING Portal Users
;; ############################################
  (def all_portal (openfile "portal-data-txt"))
  (def portal_id ((db/get-app-id "portal-data-txt") :id))
  (defn portal_name_regex [line] (get (str/split line #"\|") 0))

  (def unique_portal_users (set (map portal_name_regex all_portal)))

  (defn insert_portal_hardcoded [entry] (insert-entry "au_portal" portal_id entry "unknown" "NA" 0))

  (defn run_portal_import [] (doall (map insert_portal_hardcoded unique_portal_users)))

;; ############################################
;; IMPORTING RecReg Users
;; ############################################
  (def all_recreg (openfile "recreg-data-txt"))
  (def rec_id ((db/get-app-id "recreg-data-txt") :id))
  (defn rec_name_regex [line] [(splitter line #"," 0) (splitter line #"," 3) (splitter line #"," 2)])
  (def unique_rec_users (set (map rec_name_regex all_recreg)))

  (defn insert_rec_hardcoded [entry] (insert-entry "recreg" rec_id (get entry 0) (get entry 1)  (get entry 2) 0))

  (defn run_recreg_import [] (doall (map insert_rec_hardcoded unique_rec_users)))

;; ############################################
;; IMPORTING Alarm
;; ############################################
  (def all_alarm (openfile "titan-users-txt"))
  (def alarm_id ((db/get-app-id "titan-users-txt") :id))
  (defn insert_alarm_hardcoded [entry]
    (insert-entry "titan" alarm_id (splitter entry #"[;:]" 1) (splitter entry #"[;:]" 0) (splitter entry #"[;:]" 2) 0))

  (defn run_alarm_import [] (doall (map insert_alarm_hardcoded all_alarm)))

;; ############################################
;; IMPORTING active_directory
;; ############################################
  (def multi-parser (f/formatter (t/default-time-zone) "YYYY-MM-dd HH:mm" "YYYY/MM/dd"))
  (defn make-date [winstamp] (def long_stamp (long (/ (- (. Long parseLong winstamp) 116444736000000000) 10000)))
                                    (f/unparse multi-parser (c/from-long long_stamp)))
  (defn date-format [winstamp] (if (empty? winstamp) 0 (make-date winstamp)))

  (def ad_id ((db/get-app-id "active_directory") :id))
  (defn ldap-big-pull [username password & [attributes]]
    (let [server (client/connect host)
          qualified-name (str username "@" (-> "ausregistrygroup.local"))]
      (if (client/bind? server qualified-name  password)
        (client/search server
          "DC=ausregistrygroup,DC=local"
           {:filter (str "objectClass=user") ;"objectCategory=person")
           :attributes (or attributes [])}))))

(defn ldap-filter-pull [username password filter & [attributes]]
  (let [server (client/connect host)
        qualified-name (str username "@" (-> "ausregistrygroup.local"))]
    (if (client/bind? server qualified-name  password)
      (client/search server
                     "DC=ausregistrygroup,DC=local"
                     {:filter filter ;"objectCategory=person")
                      :attributes (or attributes [])}))))

  (defn binary-format [in-num] (reverse (pprint/cl-format nil "~b" in-num)))

  (defn ad-flagger [flag_name flag_val] (if (= flag_val \1) flag_name))

  (defn flag-vec [dec_val] (remove nil? (#(map ad-flagger % (binary-format (. Integer parseInt dec_val))) UserAccountControl_Flags)))

  (defn ldap-filter [row]
      (vector   (row :sAMAccountName)
                (str/join ", " (flag-vec (row :userAccountControl)))
                (date-format (row :lastLogon))
                (str/join "\n" (vector
                                (str "DN: " (row :distinguishedName))
                                (str "Department: " (row :department))
                                (str "Membership: " (row :memberOf))))))

  (defn insert_ad_hardcoded [entry]
    (insert-entry "active_directory" ad_id (entry 0) (entry 1) (entry 2)  0 (entry 3)))

  (defn run_ad_import [username password] (doall (map insert_ad_hardcoded (doall (map ldap-filter (ldap-big-pull username password))))))

;; ############################################
;; EXEC JOBS FROM FRONT END
;; ############################################
(defn exec-job [appid & [username password]]
  (let [val (. Integer parseInt appid)]
  (cond
    (= val unix_id)       (run_unixuser_import)
    (= val ad_id)         (run_ad_import username password)
    (= val rt_id)         (run_rt_import)
    (= val biouser_id)    (run_biolock_import)
    (= val biodev_id)     (run_biounit_import)
    (= val jde_id)        (run_jde_import)
    (= val network_id)    (run_network_import)
    (= val jira_id)       (run_jira_import)
    (= val portal_id)     (run_portal_import)
    (= val rec_id)        (run_recreg_import)
    (= val alarm_id)      (run_alarm_import)
    :else                 (throw (Exception. "Not yet implemented"))
    )))
