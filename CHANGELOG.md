# CHANGELOG.md

## Unreleased
* define answers
* define comments
* post logout
* flash for errors when register/login
* initdb.d と似たシステム
* seeding
* /admin route -- initdb.d や seed route 作戦の代わりに。


## 0.3.2-SNAPSHOT
(migrate) や lein run migrate はもちろんやるんだけど、
テーブルの最初の定義と初期データの種入れは lein の外でできると本番環境が楽か。

## 0.3.1 - 2021-10-06
* deply test onto app.melt.

## 0.3.0 - 2021-10-06

### Added
* define problems table
* seed problems (99) from `R99.html` by r99c.seed.core/seed-problems!
  FIXME: why bad using `for` for seeding? doseq is OK.

## 0.2.0 - 2021-10-04
### Added
* register
* password hash
* Logout
### Changed
* git unignore *.sql


## 0.1.1 - 2021-10-04
### Added
* gitignore .vsode/
* authentication
* access restriction

### Changed
* lein angient upgrade


## 0.1.0 - 2021-10-04
* project started.
