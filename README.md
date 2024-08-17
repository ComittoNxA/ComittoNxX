# ComittoNxX

<img src="app/src/main/res/drawable-hdpi/icon.png" width="100" align="right" alt="logo">

ComittoNxXはオープンソースの画像ビューワーです.  
対応バージョンはAndroid5.0以上です.

## 注意事項

- イメージ表示画面を開くときに落ちる場合には、画像処理スレッド数と使用メモリサイズを少なめにしてください.  
- SMB機能には多くの不具合を確認しています. SMB匿名アクセスを許可する（ログイン不要のモード）、使用中のSMBプロトコルを切り替える（例 SMB1.0 -> SMB2.0）、一度に大量のファイルを読み込まない等、各自対応をお願いします.

## ダウンロード

[Releases](https://github.com/ComittoNxA/ComittoNxX/releases)よりご利用ください.

#### [NxD](https://github.com/Kdroidwin/cnxd/tree/cnxd) からの修正点

- PNG形式で表示できないファイルがあったのを修正.

## ビルド

アプリをビルドするには [Git](https://git-scm.com/) のインストールが必要です.  
[Android Studio](https://developer.android.com/studio/install) を利用するか、gradlewコマンドでアプリをビルドしてください.

<!---
Avifのライブラリのビルドには[Ninja](https://ninja-build.org/)、[Meson](https://mesonbuild.com/)、[pkg-config](https://www.freedesktop.org/wiki/Software/pkg-config/)、[NASM](https://www.nasm.us/)のインストールが必要です.  
ライブラリのビルドまではできていますが画像の表示ができていません。  
ライブラリのビルドを有効にするには app/build.gradle 内の def WITH_AVIF = "OFF" を "ON" に変更します。
-->

## ライセンス

LICENSE ファイルに記載されたライセンスに基づきます。  

本ソースは、[ComittoNおよびComittoNxN](https://docs.google.com/open?id=0Bzx6UxEo3Pg0SXNIQVdRVnVqemM)、[ComittoNxM](https://www.axfc.net/u/3792235)、[ComittoNxT](https://www.axfc.net/u/3978158)、[ComittoNxA](https://github.com/ComittoNxA/ComittoNxA/tree/1.65A20)、[ComittoNxAC](https://www.axfc.net/u/4059552)、[ComittoNxD](https://github.com/Kdroidwin/cnxd/tree/cnxd) のソースを元にしています.
