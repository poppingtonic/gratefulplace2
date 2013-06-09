(ns gratefulplace.controllers.users
  (:require [gratefulplace.db.validations :as validations]
            [gratefulplace.db.query :as db]
            [datomic.api :as d]
            [flyingmachine.serialize.core :as s]
            [gratefulplace.db.serializers :as ss]
            [gratefulplace.db.query :as q]
            [cemerick.friend :as friend]
            cemerick.friend.workflows)
  (:use [flyingmachine.webutils.validation :only (if-valid)]
        gratefulplace.models.permissions
        gratefulplace.controllers.shared
        gratefulplace.utils))

(defserialization record ss/ent->user)

(defn registration-success-response
  [params auth]
  "If the request gets this far, it means that user registration was successful."
  (if auth {:body auth}))

(defn attempt-registration
  [req]
  (let [{:keys [uri request-method params session]} req]
    (when (and (= uri "/users")
               (= request-method :post))
      (if-valid
       params
       (:create validations/user)
       errors

       (let [user-tempid (d/tempid :db.part/user -1)
             user (serialize-tx-result
                   (q/t [(s/serialize (merge params {:id user-tempid}) ss/user->txdata)])
                   user-tempid
                   ss/ent->user)]
         (cemerick.friend.workflows/make-auth
          user
          {:cemerick.friend/redirect-on-auth? false}))
       (invalid errors)))))

(defn show
  [params]
  {:body (s/serialize
          (db/ent (str->int (:id params)))
          ss/ent->user
          {:exclude [:password]})})

(defn update-about!
  [params auth]
  (protect
   (current-user-id? (id) auth)
   (db/t [(s/serialize params ss/user->txdata {:exclude [:user/username :user/email :user/password]})])
   {:body (record (id))}))