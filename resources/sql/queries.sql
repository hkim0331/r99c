-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(sid, name, login, password)
VALUES (:sid, :name, :login, :password)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET password = :password
WHERE login= :login

-- :name get-user :? :1
-- :doc retrieves a user record given the login
SELECT * FROM users
WHERE login = :login

-- :name delete-user! :! :n
-- :doc deletes a user record given the login
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
SET num = :num, problem = :problem, test = :test,
     update_at = now()
WHERE id = :id

-- :name get-problem :? :1
-- :doc retrieves a problem record given the num
SELECT * FROM problems
WHERE num = :num

-- :name delete-problem! :! :n
-- :doc deletes a problem record given the id
DELETE FROM problems
WHERE id = :id

-- :name problems :? :*
-- :doc get all problems
SELECT * from problems order by num

-- :name delete-problems-all! :! :n
-- :doc delete all from problems table
DELETE FROM problems

-- :name create-answer! :! :n
-- :doc creates a new problem record
INSERT INTO answers
(login, num, answer, md5)
VALUES (:login, :num, :answer, md5)

-- :name get-answer :? :1
-- :doc retrieves an answer to `num` from user `login`
SELECT * FROM answers
WHERE num = :num and login = :login

