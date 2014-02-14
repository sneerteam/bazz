(ns bazz.spikes.tentacles
  #_(:use tentacles.core)
  (:require tentacles.repos))

(first (tentacles.repos/user-repos "amalloy"))

