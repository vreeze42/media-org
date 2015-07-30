(use '(lobos connectivity core schema))

;; hier snapt 'ie ~ nog niet, dus volledig.
;; leiningen-exec snapt 'ie ook niet, dus functies maar even los definieren.
;; (load-file "/home/nico/nicoprj/clojure/lib/def-libs.clj")

(require '[clojure.java.io :as io] 
         '[clojure.string :as str]
         '[me.raynes.fs :as fs]
         '[clojure.java.jdbc :as jdbc]
         '[clojure.tools.cli :refer [cli]]
         '[swiss.arrows :refer :all]            ; bij deze refer all wel handig, met -<> operators, wil hier geen namespace voor.
         '[clojure.tools.logging :as log]
         '[clj-logging-config.log4j :as logcfg]
         '[clojure.edn :as edn]
         '[clj-time.core :as t]
         '[clj-time.coerce :as tc]
         '[clj-time.format :as tf])

(defn load-config
  "Given a filename, load & return a config file"
  [filename]
  (edn/read-string (slurp filename)))

(defn db-postgres
  "Create db-spec for postgres based on dbname, user, pw"
  ([db-name user password]
     {:classname "org.postgresql.Driver" ; must be in classpath
      :subprotocol "postgresql"
      :subname (str "//localhost:5432/"  db-name)
                                        ; Any additional keys are passed to the driver
                                        ; as driver-specific properties.
      :user user
      :password password})
  ([dbspec]
     "Create a postgres db spec using an EDN file"
     (let [cfg (load-config dbspec)]
       (db-postgres (:database cfg) (:user cfg) (:password cfg)))))

(def edn-spec "~/.config/media/media.edn")

(def dbspec (db-postgres (fs/expand-home edn-spec)))

(open-global dbspec)

(defn surrogate-key [table]
  (integer table :id :auto-inc :primary-key))

;; butlast verwijderd, wil wel directory_id als veldnaam.
(defn refer-to [table ptable]
  (let [cname (-> (->> ptable name (apply str))
                  (str "_id")
                  keyword)]
    (integer table cname [:refer ptable :id :on-delete :set-null])))

(defmacro tbl [name & elements]
  `(-> (table ~name
              (surrogate-key))
       ~@elements))

(create 
 (tbl :directory 
      (varchar :fullpath 1023 :unique)
      (varchar :parent_folder 1023)
      (integer :parent_id)
      (varchar :computer 30)))

;; dit werkt, meteen een directory tabel in de DB.

;; hier een migration van te maken?
(print-stash)
;; ok, geeft nog tbl, dus macro niet expanded.

(generate-migration 'create-directory)
;; werkt, maar lobos/migrations.clj moest al wel bestaan, en eerste regel met ns ook
;; hierna is stash leeg, print-stash toont nil.

(alter :add (table :file
                   (refer-to :directory)))

(generate-migration 'add-file-directory)
;; nu eerst fout, hij kent defmigration niet.
;; door goede dingen te use-n, werkt het wel.


