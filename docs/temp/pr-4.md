## 関連Issue
Closes #4

## 変更内容
- Spring Security（`spring-boot-starter-security`、`thymeleaf-extras-springsecurity6`、`spring-boot-starter-security-test`）を有効化し、認証・認可設定を追加した。
- クラスの責務に応じたパッケージ構成（`model`, `controller`, `security`）へ整理した。
  - `model`: `User.java`, `UserMapper.java` を移動・配置。
  - `controller`: `AuthController.java` を移動し、ログイン画面を表示する `GET /login` エンドポイントを追加。
  - `security`: 認可およびログイン画面のルーティングを行う `SecurityConfig.java` と、登録済みユーザー情報から動的に認証を行う `CustomUserDetailsService.java` を追加した。
- ログイン画面 `templates/login.html` を作成し、認証情報入力フォームとエラーメッセージ表示領域を実装した。
- `templates/register.html` のフォーム送信先を `th:action="@{/register}"` に修正し、CSRF保護に対応した。
- ログイン成功・失敗、エラー表示を検証する自動テスト `UserLoginTests.java` を追加した。
- 既存のユーザー登録テスト `UserRegistrationTests.java` をCSRF対応および新パッケージ配置に合わせて更新した。

## AC確認結果
- AC1（登録済み利用者のログイン成功とタスク一覧ゾーンへの移動）:
  - 登録済みのユーザー名とパスワードを入力してログインを実行し、認証状態となってタスク一覧ゾーン `/` へリダイレクトされることを自動テスト（`UserLoginTests.登録済みユーザーでログインすると認証されタスク一覧へリダイレクトされる`）およびブラウザ操作で確認した。
- AC2（未登録・パスワード不一致時の認証失敗とエラー表示）:
  - 未登録のユーザー名または誤ったパスワードを入力してログインを実行した際、認証されずに `/login?error` へ遷移し、ログイン画面上に「ユーザー名またはパスワードが正しくありません」とエラー表示されることを自動テスト（`UserLoginTests.未登録ユーザーでログインすると認証失敗しログイン画面へリダイレクトされる`、`UserLoginTests.パスワード不一致でログインすると認証失敗しエラー表示を確認できる`）およびブラウザ操作で確認した。

## テスト結果
- `./gradlew test` を実行し、`BUILD SUCCESSFUL`（4つのタスク実行、全テスト成功）を確認した。
  - `UserLoginTests`: ログイン画面表示、ログイン成功時のリダイレクト・認証状態確認、未登録・パスワード不一致時の認証失敗・エラー表示確認
  - `UserRegistrationTests`: 登録画面表示、新規登録、重複登録拒否の確認
  - `Z14400ApplicationTests`: コンテキスト起動確認
- `./gradlew bootRun` を実行し、ブラウザ上で登録後のユーザーによるログイン成功および誤ったパスワードでのエラー表示を確認した。
- 残る懸念: パスワードは現時点では平文（`{noop}`）照合を行っており、本番運用に向けたハッシュ化（BCrypt等）は今後のIssueで必要に応じて検討する。
