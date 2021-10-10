# CHANGELOG.md

## Unreleased
* define comments table
* post logout
* flash for errors when register/login
* login せずに /admin/ を叩いた場合に x is null エラー。
* renumber
* problems の表示に、C のソースをデコレートして表示できないか？
  markdown なら以下ができれば十分だが。
```c
int func_test(void) {
  return 1==1 && 2==2 && 3==3;
}
```
* answer をボタンに。button is-primary is-small でもやや大きすぎ、ブサイク。
https://bulma.io/documentation/overview/colors/
* login/regisger の説明書き
* すでに付けた回答を表示できてない。
* syntax check だけする。

## 0.4.0-SNAPSHOT
comments 方面。


## 0.3.4 - 2021-10-10
* status problems に色つけ
* /answer-page: 過去回答を md5 でグルーピング表示。自分の回答は same md5 に入るやろ。
* 同じ問題への回答の分類は group-by で。

## 0.3.3  - 2021-10-10
### Added
* problems ... defined table and a route /problems
* answers ... defined table and a route /answer:num
* /status

## 0.3.2 - 2021-10-09
### Added
* /admin/ ... seed problems ボタン。タネいれ。
* /admin/problems ... 問題の表示と編集。
* /admin route -- initdb.d や seed route 作戦の代わりに。
* seeding

本番では lein run migrate の後、
管理者を作成し、ログイン、
/admin/ から問題を入れる。

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
