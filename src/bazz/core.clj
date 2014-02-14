(ns bazz.core
  (:require [clojure.java.io :as io])
  (:require [tentacles.repos :as repos]))

(defn user-home []
  (System/getProperty "user.home"))

(def products-root
  (str (user-home) "/sneer/products"))

(defn peer-product-path [peer product]
  (str (user-home) "/sneer/peers/" peer "/" product))

(defn list-subfolders [^String folder-name]
  (filter #(.isDirectory %) (-> folder-name io/file .listFiles)))

(defn product-folders []
  (list-subfolders products-root))

(defn is-git [folder]
  (.exists (java.io.File. folder ".git")))

(defn status [folder]
  (if (is-git folder)
    :shared
    :new))

(defn product-list []
  (map #(hash-map :name (.getName %) :status (status %)) (product-folders)))

(defn peer-product-list [peer-login]
  (map #(assoc % :peer peer-login) (remove empty? (repos/user-repos peer-login))))

; (product-list)

;  [{:status :new, :name "Javatari 2.0"}
;   {:status :modified, :name "Emacs for Clojure"}])
