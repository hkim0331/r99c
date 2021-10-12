# CHANGELOG.md

## Unreleased
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
* パスワードを変えられないと。
* 問題修正 ... test コラムを有効に。
* /admin/users ... ユーザを一覧表示し、パスワードを初期化、エントリーを削除する。
* /admin/comments ... 何をする予定だったっけ？
* コピペ予防 css
* restrict 10 answers a day

## 0.6.1 - 2021-10-12
* improve validate-answer and create-answer!
* version 表示 in /about div 要素だけまとめて左寄せしたい。

## 0.6.0 - 2021-10-11
### Added
* syntax check, but not flash back.

## 0.5.1 - 2021-10-11
### Added
* /register にバリデーション
* regisger の説明書き
### Changed
* /answer/:id /comment/:id で表示する問題文を |safe でフィルタした。

## 0.5.0 - 2021-10-11
* r99.melt にテスト配置
### Added
* defined comments table
* /comment/:id -- id は answers.id
* can add comments
### Changed
* answer-form.html: s/Answer to/New Answer to/
* Navbar: /Home の代わりに /problems をリンク
* problems.html: {{p.problem|safe}}.
内容の修正は docs/seed-problems.html でやらないと本番に反映しない。

## 0.4.0 - 2021-10-11
* 回答を表示できるようになった。エンドポイントは /comment/:id. 回答を表示するとは
すなわち、コメントできるってことだ。
* answer をボタンに。button is-primary is-small でもやや大きすぎ、ブサイク。
https://bulma.io/documentation/overview/colors/

## 0.3.5 - 2021-10-10
* VScode バグ？操作ミス？ src/clj/r99croutes/home.clj が CHANGELOG.md の内容で
上書きになった。master からチェックアウトした home.clj で develop を上書き。
操作はこれでいいのかな？ 0.3.5 でコミットする。

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
* FIXME: why bad using `for` for seeding? doseq is OK.

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
