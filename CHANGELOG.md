# CHANGELOG.md

## Unreleased
* post logout
* problems の表示に、C のソースをデコレートして表示する。
  markdown なら以下ができれば十分だが。
```c
int func_test(void) {
  return 1==1 && 2==2 && 3==3;
}
```
* /admin/users ... ユーザを一覧表示し、パスワードを初期化、エントリーを削除する。
* /admin/comments ... 何をする予定だったっけ？
* /comment, comment のフォーマット...どうしろと？もっと具体的に書いておかないと忘れる。
* limit answers a day
* routes/ にルート以外のロジックを入れ込みすぎ。
* recent 10 の他に、top 10
* 積分グラフ（折れ線）は本日まででプロットを止めないと変なグラフになる。
* count of solved problems


## 0.7.1 - 2021-10-19
### Changed
- stop to use capitalize, login|capitalize, in status.html
### Added
- who send the same answer with you?


## 0.7.0 - 2021-10-18
- feature/svg-plot ブランチで開発中。date オブジェクトだと、
org.java.Time.DateTime と java.time.LocalDate の見かけは一緒でもマッチは取れるか？
面倒だ、文字列に変換してしまおう。
### Fixme
- HugSQL で `select date '2021-10-10' + integer '135'` の書き方がわからない。
### Changed
- return date as string rather than date object.
### Added
- can plot SVG graphs.

## 0.6.11 - 2021-10-18
### Bugfix
- 0.6.10 is a mistake. bug fixed in branch svg-plot.

## 0.6.10
enbug. 500 error  when access /admin/
hotfix 0.6.10 start.
### Bugfix
-- simply forgot `name login`. fixed.

### BUG
## 0.6.9
* 旧 r99 から favicon.ico をコピー。
## Changed
* middleware/admin? (get-in request [:session :identity]) が null になるケース。

## 0.6.8 - 2021-10-18
* defined/installed r99c.service
* /answer/:n で回答リンクの表示を抑制しない。リンク先で制限かける。
リンクをたどろうとしたらエラーの方がいい。

## 0.6.7 - 2021-10-17
* display individual/class answers with SVG graph.
一旦、feature/class-svg をマージして出直そう。
グラフの横軸は、個人、クラスとも、ゴールの日までの日にちとする。
クラスはその日の回答数、個人は回答数の積分値とする。ゴールは 99 題。
### Added
* /db-dumps フォルダ。データベースのダンプと、ダンプ・リストアスクリプト。
gitignore する。
* link to qa.melt from navbar.
* /comment/:n で create_at 表示。

## 0.6.6 - 2021-10-17
### Fixed
* 回答つけてないユーザは /comment/:n を見れない。home/comment-from に細工。403 を返す。

## 0.6.5 - 2021-10-17
### Added
- recent answers (logins)
- link from recents
- /comment/:n から他の回答も見れた方がいい。

## 0.6.4 - 2021-10-12
## Changed
* improve status.html individual field, class field, sent comments column
* copy protect CSS
https://on-ze.com/archives/5744

## 0.6.3 - 2021-10-12
### Added
* db/answers-by-date ... how many answers in a day?
* db/answers-bu-date-login ... how many answers user `login` submited in a day?

## 0.6.2 - 2021-10-12
### Added
* /ch-pass to change password.

## 0.6.1 - 2021-10-12
### Changed
* improve validate-answer and create-answer!
### Added
* version 表示 in /about. div 要素だけまとめて左寄せしたい。

## 0.6.0 - 2021-10-11
### Added
* syntax check, but not flash back.

## 0.5.1 - 2021-10-11
### Added
* validation for /register
* doc for /regisger
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
