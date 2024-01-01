(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [visitera.core :refer [start-app]]
   [visitera.db.core :refer [conn delete-database install-schema]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'visitera.core/repl-server)
  (install-schema conn))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'visitera.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn reset-db
  "Delete database and restart application"
  []
  (delete-database)
  (restart))

(comment
  (start-app))
