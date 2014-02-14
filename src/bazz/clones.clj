(ns bazz.clones
  (:require   [bazz.core :as core]
              [clojure.java.io :as io]
              [clojure.core.async :as async :refer [chan >!! <!! alts!! timeout thread close! go]])
  (:import [org.eclipse.jgit.api Git]
           [org.eclipse.jgit.lib EmptyProgressMonitor]))


(defn clone-with-progress-monitor
  ([pm uri local-dir]
   (clone-with-progress-monitor pm uri local-dir "origin" "master" false))
  ([pm uri local-dir remote-name]
   (clone-with-progress-monitor pm uri local-dir remote-name "master" false))
  ([pm uri local-dir remote-name local-branch]
   (clone-with-progress-monitor pm uri local-dir remote-name local-branch false))
  ([pm uri local-dir remote-name local-branch bare?]
   (-> (Git/cloneRepository)
       (.setURI uri)
       (.setDirectory (io/as-file local-dir))
       (.setRemote remote-name)
       (.setBranch local-branch)
       (.setBare bare?)
       (.setProgressMonitor pm)
       (.call))))

(defn simple-monitor [monitor-channel]
  (proxy [EmptyProgressMonitor] []
    (beginTask [taskName _]
               (>!! monitor-channel taskName))
    (update [_]
              (>!! monitor-channel " ."))
    (endTask []
               (>!! monitor-channel "\n"))
    (isCancelled []
                 false)))

(defn clone-with-simple-monitor [uri local-dir monitor-channel]
  (try
    (clone-with-progress-monitor (simple-monitor monitor-channel) uri local-dir)
    (catch Exception e
      (>!! monitor-channel (str "Error: " (.getMessage e))))))

(def responses-by-product-path (atom {}))

(defn github-uri [peer product]
  (format "git@github.com:%s/%s.git" peer product))

(defn assoc-if-absent! [map-in-atom key new-value]
  (loop []
    (if-let [value (get @map-in-atom key)]
      value
      (do
        (swap! map-in-atom #(if (get % key) % (assoc % key new-value)))
        (recur)))))

(defn serve-clone-request [peer product local-dir response-channel]
  (let [new-channel (async/chan 100)
        new-mult (async/mult new-channel)
        mult (assoc-if-absent! responses-by-product-path local-dir new-mult)]
    (async/tap mult response-channel)
    (if (identical? mult new-mult)
      (thread
        (clone-with-simple-monitor (github-uri peer product) local-dir new-channel)
        (swap! responses-by-product-path #(dissoc % local-dir))
        (>!! new-channel " Done.")
        (close! new-channel)))))
