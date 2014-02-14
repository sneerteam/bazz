(ns bazz.spikes.jgit
  (:use clj-jgit.porcelain))

(def repo-name "trash")
(def repo-folder (str "tmp/" repo-name))
(def repo-url (str "git@github.com:klauswuestefeld/" repo-name ".git"))

(fs.core/delete-dir repo-folder)

(with-identity {repo-name {}}
    (def my-clone (git-clone-full repo-url repo-folder))
    (def pull-result (-> my-clone :repo .pull .call)))

(.isSuccessful pull-result)

(def my-repo (:repo my-clone))

(git-status my-repo)


;; List existing branches
(git-branch-list my-repo)

;; Create a new local branch to store our changes
(git-branch-create my-repo "my-branch")

;; Prove to ourselves that it was created
(git-branch-list my-repo)

;; Check out our new branch
(git-checkout my-repo "my-branch")

;; Now go off and make your changes.
(with-open [f (clojure.java.io/writer (str repo-folder "/whatever.txt"))]
  (.write f "test"))

;; For example, let's say we added a file "foo.txt" at the base of the project.
(git-status my-repo)

;; Add the file to the index
(git-add my-repo "whatever.txt")
;=> #<DirCache org.eclipse.jgit.dircache.DirCache@81db25>

;; Check for status change
(git-status my-repo)

;; Now commit your changes, specifying author and committer if desired
(git-commit my-repo "Add a file" {:name "Fulano" :email "fulano@example.com"})

;; Status clean
(git-status my-repo)

(git-clean my-repo :clean-dirs? true, :ignore? true)

(first (git-log my-repo))

(def final-push
  (with-identity {"trash" {}}
    (-> my-repo .push .call)))

(-> final-push first .getRemoteUpdates first)
