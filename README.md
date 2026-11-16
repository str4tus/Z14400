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

起動後、以下を開いてください。

- ログイン: http://localhost:8080/login
- ユーザー登録: http://localhost:8080/register
- タスク一覧: http://localhost:8080/

終了するには `Ctrl+C` を押します。

## 主な機能

- タスクの登録
- タスク一覧の確認
- タスクの更新
- タスクの状態変更
- 自分が作成したタスクの削除

詳細は [共通ユーザーマニュアル](docs/user-manual.md) を参照してください。
