(ns lobos.migrations
  (:require [lobos.migration :as lbm] ;; allemaal als lb? anders m,s,c erachter.
            [lobos.schema :as lbs] ;; hier staat integer in. hier zit boolean in, deze dus niet includen?
            [lobos.core :as lbc]
            [lobos.connectivity :as lbcn]
            [clojure.edn :as edn]
            [me.raynes.fs :as fs]))
;; aanmaken van migration ging goed, ondanks de boolean in lobos.schema.

;; db def hier voorlopig ook in, mogelijk later in db.clj en die dan includen
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

;; niet standaard db openen, wel doen als je nieuwe tabellen wilt maken.
;; (open-global dbspec)

;; (lbc/print-stash)

(defn surrogate-key [table]
  (lbs/integer table :id :auto-inc :primary-key))

;; butlast verwijderd, wil wel directory_id als veldnaam.
(defn refer-to [table ptable]
  (let [cname (-> (->> ptable name (apply str))
                  (str "_id")
                  keyword)]
    (lbs/integer table cname [:refer ptable :id :on-delete :set-null])))

(defmacro tbl [name & elements]
  `(-> (lbs/table ~name
              (surrogate-key))
       ~@elements))

;; deze gegenereerd met (generate-migration 'create-directory)
;; was wel nodig dat clj file al bestond en eerste regel er in stond.
;; de macro hier nog gehandhaaft.
;; TODO macro's en functies hierbij?
(lbm/defmigration
 create-directory
 (up
  []
  (lbc/create
   (tbl
    :directory
    (lbs/varchar :fullpath 1023 :unique)
    (lbs/varchar :parent_folder 1023)
    (lbs/integer :parent_id)
    (lbs/varchar :computer 30))))
 (down [] (drop (tbl :directory))))

(lbm/defmigration
 add-file-directory
 (up
  []
  (lbc/alter :add (lbs/table :file (refer-to :directory))))
 (down
  []
  (lbc/alter :drop (lbs/table :file (refer-to :directory)))))

;; deze heeft geen down-migration, want hij snapt lbc/create niet.
(defmigration
 add-book-and-author
 (up
  []
  (lbc/create
   (tbl
    :author
    (lbs/varchar :fullname 200 :unique)
    (lbs/varchar :firstname 100)
    (lbs/varchar :lastname 100)
    (lbs/text :notes)))
  (lbc/create
   (tbl
    :book
    (lbs/varchar :title 200)
    (lbs/varchar :edition 10) ; of integer? (deze comment ook bij maken erbij, maar neemt 'ie niet over)
    (lbs/varchar :authors 1023)
    (lbs/varchar :publisher 100)
    (lbs/date :pubdate)
    (lbs/varchar :isbn10 15) ; extra ruimte voor streepjes
    (lbs/varchar :isbn13 20) ; deze ook.
    (lbs/integer :npages)
    (lbs/varchar :language 30)
    (lbs/text :notes)
    (lbs/varchar :tags 1023)))
  (lbc/create
   (tbl
    :bookauthor
    (refer-to :book)
    (refer-to :author)
    (lbs/text :notes)))))
