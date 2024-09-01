# ComittoNxX

<img src="app/src/main/res/drawable-hdpi/icon.png" width="100" align="right" alt="logo">

ComittoNxXはオープンソースの画像ビューワーです.  
対応バージョンはAndroid5.0以上15までです.

- 対応する電子書籍ファイルの形式：  
  PDF、EPUB(画像のみ)、TEXT、青空文庫  

- 対応する画像ファイルの形式：  
  JPEG、GIF、PNG、WebP、AVIF、HEIF(Android 8.0以上)  

- 対応する圧縮ファイルの形式：  
  ZIP、RAR  

- Windowsファイル共有(SMB)に対応  

## 注意事項

- イメージ表示画面を開くときに落ちる場合には、画像処理スレッド数と使用メモリサイズを少なめにしてください.
- SMB機能には多くの不具合を確認しています. SMB匿名アクセスを許可する（ログイン不要のモード）、使用中のSMBプロトコルを切り替える（例 SMB1.0 -> SMB2.0）、一度に大量のファイルを読み込まない等、各自対応をお願いします.

## ダウンロード

[Releases](https://github.com/ComittoNxA/ComittoNxX/releases) よりご利用ください.

#### [NxD](https://github.com/Kdroidwin/cnxd/tree/cnxd) からの修正点

- PNG形式で表示できないファイルがあったのを修正.
- AVIF形式画像の表示に対応.
- HEIF形式画像の表示に対応.(Android 8.0以上)
- PDF形式電子書籍の表示に対応.

## ビルド

アプリをビルドするには [Git](https://git-scm.com/)、[Ninja](https://ninja-build.org/)、[Meson](https://mesonbuild.com/)、[pkg-config](https://www.freedesktop.org/wiki/Software/pkg-config/)、[NASM](https://www.nasm.us/) のインストールが必要です.  
[Android Studio](https://developer.android.com/studio/install) を利用するか、gradlewコマンドでアプリをビルドしてください.

<details><summary><b>ビルドに必要なファイルの作成手順</b></summary>
<p>

###### Android SDKインストールパスの設定

Android studio の場合は [File] > [Project Structure] > [SDK Location] から設定します.
インストールから実行する場合は [Tools] > [SDK Manager] から実行します.

###### 署名の作成

キーストアファイルを作成して保存します.  
Android studio の場合は [Build] > [Generate Signed Bundle/APK] から作成します.

###### signingConfigs/release.gradle の作成

プロジェクトルートに signingConfigs というフォルダを作成します.  
signingConfigs の中に release.gradle というファイルを作成します.

```gradle
signingConfigs {
    release {
        storePassword '${署名ファイルのパスワード}'
        keyPassword '${鍵のパスワード}'
        storeFile file('${署名のファイル名}')
        keyAlias '${鍵のエイリアス}'
    }
}
```
</details>

## ライセンス

LICENSE ファイルに記載されたライセンスに基づきます。

本ソースは、[ComittoNおよびComittoNxN](https://docs.google.com/open?id=0Bzx6UxEo3Pg0SXNIQVdRVnVqemM)、[ComittoNxM](https://www.axfc.net/u/3792235)、[ComittoNxT](https://www.axfc.net/u/3978158)、[ComittoNxA](https://github.com/ComittoNxA/ComittoNxA/tree/1.65A20)、[ComittoNxAC](https://www.axfc.net/u/4059552)、[ComittoNxD](https://github.com/Kdroidwin/cnxd/tree/cnxd) のソースを元にしています.
