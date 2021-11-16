-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(sid, name, login, password)
VALUES (:sid, :name, :login, :password)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET password = :password, update_at = CURRENT_TIMESTAMP
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

-- ----------------
-- problems section
-- ----------------

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

-- --------------
-- answers section
-- --------------

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
SELECT create_at::date::text, count(*) FROM answers
GROUP BY create_at::date
ORDER BY create_at::date

-- :name answers-by-date-login :? :*
-- :doc how may answers by login?
SELECT create_at::date::text, count(*) FROM answers
where login = :login
GROUP BY create_at::date
ORDER BY create_at::date

-- :name recent-answers :? :*
-- :doc fetch recent n answers
SELECT * FROM answers
WHERE login != 'hkimura'
ORDER by id DESC
limit :n

-- :name answers-same-md5 :? :*
-- :doc get answers which has same MD5 value
SELECT * FROM answers
WHERE md5 = :md5

-- :name top-users :? :*
-- :doc get top n users
SELECT login, count(num) FROM answers
WHERE login != 'hkimura'
GROUP BY login
ORDER BY count(num) DESC limit :n

-- :name top-users-distinct :? :*
-- :doc get top n users order by distinct(num)
SELECT login, count(distinct(num)) FROM answers
WHERE login != 'hkimura'
GROUP BY login
ORDER BY count(distinct(num)) DESC limit :n

-- ----------------
-- comments section
-- ----------------

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
ORDER BY id

-- :name sent-comments :? :1
-- :doc how many comments user `login` sent?
SELECT count(*) FROM comments
WHERE from_login = :login

-- :name sent-comments-days :? :*
-- :doc how many comments `loign` sent in days?
SELECT create_at::date::text, count(*) FROM comments
WHERE from_login= :login
GROUP BY create_at::date
ORDER BY create_at::date;

-- :name comments-rcvd :? :*
-- :doc list of comments received
SELECT * from comments
WHERE to_login = :login
ORDER BY create_at DESC;

-- :name comments-from :? :*
-- :doc retrieve all comments
SELECT from_login, count(*) from comments
GROUP BY from_login
ORDER BY count(*) DESC;

-- :name comments-to :? :*
-- :doc retrieve all comments
SELECT to_login, count(*) from comments
GROUP BY to_login
ORDER BY count(*) DESC;

-- :name comments-sent :? :*
-- :doc  comments sent from from_login
SELECT * FROM comments
WHERE from_login = :login
ORDER BY create_at DESC;

-- :name recent-comments :? :*
-- :doc retrieve recent n comments
SELECT * FROM comments
ORDER BY create_at DESC
limit :n
