(ns gratefulplace.controllers.likes
  (:require [datomic.api :as d]
            [gratefulplace.db.query :as db]
            [gratefulplace.db.maprules :as mr]
            [flyingmachine.cartographer.core :as c])
  (:use [liberator.core :only [defresource]]
        gratefulplace.controllers.shared
        gratefulplace.models.permissions
        gratefulplace.utils))

(defn find-like
  [params auth]
  (db/one [:like/post (str->int (:post-id params))]
          [:like/user (:id auth)]))

(defresource create! [params auth]
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :authorized? (logged-in? auth)

  :post! (fn [_]
           (if-not  (find-like params auth)
             (db/t [{:db/id #db/id[:db.part/user]
                     :like/user (:id auth)
                     :like/post (str->int (:post-id params))}])))
  :handle-created "")

(defresource delete! [params auth]
  :allowed-methods [:delete]
  :available-media-types ["application/json"]
  :authorized? (fn [_]
                 (let [like (find-like params auth)]
                   (if like
                     {:record {:id (:db/id like)}})))
  :exists? exists-in-ctx?
  :delete! (fn [ctx]
             (db/retract-entity (get-in ctx [:record :id]))))