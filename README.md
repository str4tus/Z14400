# Z14400 タスク管理アプリ

## セットアップ

### 必要な環境

- Git
- Java 25

### リポジトリの取得

```bash
git clone https://github.com/str4tus/Z14400.git
cd Z14400
```

### アプリの起動

Windowsでは次を実行します。

```powershell
.\gradlew.bat bootRun
```

Git Bashでは次を実行します。

```bash
./gradlew bootRun
```

ポート80で起動するため、Windowsでは管理者権限で実行する必要がある場合があります。ポート80が別のアプリケーションで使用中の場合は、使用中のプロセスを停止してから起動してください。

起動後、以下を開いてください。

- ログイン: http://localhost/login
- ユーザー登録: http://localhost/register
- アプリケーション: http://localhost/

終了するには `Ctrl+C` を押します。

## 主な機能

- グループの作成と参加
- グループごとのタスク一覧、登録、編集
- 日時未入力、片方のみのタスク日時入力
- グループ管理者による解散と全タスク削除
- メンバーによる自分のタスク削除

詳細は [共通ユーザーマニュアル](docs/user-manual.md) を参照してください。
