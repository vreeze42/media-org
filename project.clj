(defproject media-org "0.1.0-SNAPSHOT"
  :description "Media organisation"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [postgresql "9.3-1102.jdbc41"]
                 [lobos "1.0.0-beta3"]
                 [me.raynes/fs "1.4.5"]                ; file system utilities
                 [org.clojure/tools.cli "0.2.4"]
                 [swiss-arrows "1.0.0"]
                 [org.clojure/tools.logging "0.3.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.5"]     ; coupling tools.logging with log4j.
                 [log4j/log4j "1.2.16"]                ; in test-log4.clj deze niet, kan het kwaad?
                 [clj-logging-config "1.9.10"]
                 [clj-time "0.8.0"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler media-org.handler/app
         :init media-org.handler/init
         :destroy media-org.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}})
