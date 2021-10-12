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

-- :name problems-count :? :1
-- :doc returns problems count
SELECT COUNT(*) FROM problems

-- :name create-answer! :! :n
-- :doc creates a new problem record
INSERT INTO answers
(login, num, answer, md5)
VALUES (:login, :num, :answer, :md5)

-- :name get-answer :? :1
-- :doc retrieves the most recent answer to `num` from user `login`
SELECT * FROM answers
WHERE num = :num and login = :login
ORDER BY id DESC

-- :name answers-by :? :*
-- :doc retrieve all answers solved by user `login`
SELECT * FROM answers
WHERE login = :login
ORDER BY num

-- :name answers-to :? :*
-- :doc retrieve all answers to `num`, chronological order.
SELECT * FROM answers
WHERE num = :num
ORDER BY id

-- :name get-answer-by-id :? :1
-- :doc retrieve answer by `id`
SELECT * FROM answers
WHERE id = :id

-- :name answers-by-date :? :*
-- :doc how many answers in dates?
SELECT create_at::date, count(*) FROM answers
GROUP BY create_at::date
ORDER BY create_at::date

-- :name answers-by-date-login :? :*
-- :doc how may answers by login?
SELECT create_at::date, count(*) FROM answers
where login = :login
GROUP BY create_at::date
ORDER BY create_at::date

-- :name create-comment! :! :n
-- :doc create a comment on problem number num, answer id a_id
INSERT INTO comments
(comment, from_login, to_login, p_num, a_id)
VALUES
(:comment, :from_login, :to_login, :p_num, :a_id)

-- :name get-comments :? :*
-- :doc retrieve comments to answer id a_id
SELECT * FROM comments
WHERE a_id = :a_id
