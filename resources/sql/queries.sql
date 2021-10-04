-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(sid, name, login, password)
VALUES (:sid, :name, :login, :password)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET (password = :password)
WHERE login= :login

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE login = :login

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE login = :login

-- :name users :? :*
-- :doc get all users
SELECT * from users;
