(defproject bazz "1.0.0-SNAPSHOT"
  :description "A Github client to share code easier."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-jgit "0.6.4"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.6"]    ; URL request routing
                 [enlive "1.1.5"]       ; HTML templating.
                 [javax.servlet/servlet-api "2.5"]
                 [tentacles "0.2.5"]    ; Github API
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/data.json "0.2.4"]]

  :main bazz.main)
