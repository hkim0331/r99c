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

-- :name create-problem! :! :n
-- :doc creates a new problem record
INSERT INTO problems
(num, problem)
VALUES (:num, :problem)

-- :name update-problem! :! :n
-- :doc updates an existing problem record
UPDATE problems
SET (num = :num, is_avail = :is_avail, problem = :problem, test = :test,
     update_at = now())
WHERE id = :id

-- :name get-problem :? :1
-- :doc retrieves a problem record given the num
SELECT * FROM problems
WHERE id = :id

-- :name delete-problem! :! :n
-- :doc deletes a problem record given the num
DELETE FROM problems
WHERE id = :id

-- :name problems :? :*
-- :doc get all problems
SELECT * from problems;