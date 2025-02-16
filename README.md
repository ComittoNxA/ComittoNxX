# ComittoNxX

<img src="app/src/main/res/drawable-hdpi/comittonxx.png" width="100" align="right" alt="logo">

ComittoNxX はオープンソースの画像ビュワーです.  
対応バージョンは Android5.0 以上 15 までです.

- 対応する電子書籍ファイルの形式：  
  PDF、EPUB、TEXT、青空文庫

- 対応する画像ファイルの形式：  
  JPEG、GIF、PNG、WebP、AVIF、HEIF(Android 8.0以上)、JXL

- 対応する圧縮ファイルの形式：  
  ZIP、RAR

- 対応する共有オンラインストレージサービス：
  - Windowsファイル共有(SMB)
  - 外部アプリを介した共有オンラインストレージへのアクセス  
    (一応動作はしますが、組み合わせによっては使い物になりません)
    - ストレージアクセスフレームワーク(SAF)
      - CIFS Documents Provider : [Google Play](https://play.google.com/store/apps/details?id=com.wa2c.android.cifsdocumentsprovider)、[マニュアル](https://github.com/wa2c/cifs-documents-provider/wiki/Manual-ja)  
        SMB、FTP、FTPS、SFTPをサポート.
      - Round Sync : [GitHub](https://github.com/newhinton/Round-Sync)  
        多くのクラウド ストレージ プロバイダーに対応.
    - ファイルピッカー
      - Google ドライブ
      - Microsoft OneDrive
      - その他メーカー純正アプリ等

    アカウントの認証などは外部アプリ側で設定してください.  
    これらの外部アプリは起動時に自動実行やバックグラウンドで停止しないなどの設定を推奨します.

<!-- 
> [!IMPORTANT]
> 更新を再開しました.
-->

<!-- 
> [!NOTE]
> 掲示板への投稿が規制されたためバージョン更新の告知を停止しています.
-->

## 注意事項

- 32bit版アプリや32bitOSを使用している場合は使用メモリサイズを少なめにしてください.
- SMBの接続情報にドメイン名を入力すると初回接続が遅くなる場合があります.  不要なドメイン名(WORKGROUP等)の使用は避けてください.
- オンラインストレージ上のRAR圧縮形式ファイルの構造解析は時間がかかります.サムネイル作成時にファイル名を解析する場合にはZIP圧縮形式の使用を推奨します.

## 既知の不具合

- アプリ内ヘルプの内容が古いです.
- Android 10 でローカルファイルの削除と名前の変更ができません.

## ダウンロード

[Releases](https://github.com/ComittoNxA/ComittoNxX/releases) よりご利用ください.

#### [NxD](https://github.com/Kdroidwin/cnxd/tree/cnxd) からの修正点

- PNG形式で表示できないファイルがあったのを修正.
- AVIF形式画像の表示に対応.
- HEIF形式画像の表示に対応.(Android 8.0以上)
- JXL形式画像の表示に対応.
- PDF形式電子書籍の表示に対応.
- EPUB形式電子書籍の本文テキストの表示に対応.

## ビルド

アプリをビルドするには [Git](https://git-scm.com/)、[Ninja](https://ninja-build.org/)、[Meson](https://mesonbuild.com/)、[pkg-config](https://www.freedesktop.org/wiki/Software/pkg-config/)、[NASM](https://www.nasm.us/) のインストールが必要です.  
[Android Studio](https://developer.android.com/studio/install) を利用するか、gradlewコマンドでアプリをビルドしてください.

<details><summary><b>ビルドに必要なファイルの作成手順</b></summary>
<p>

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

LICENSE ファイルに記載されたライセンスに基づきます.  
内部で使用しているライブラリのライセンスは各ライブラリの指定に従います.  

本ソースは、[ComittoNおよびComittoNxN](https://docs.google.com/open?id=0Bzx6UxEo3Pg0SXNIQVdRVnVqemM)、[ComittoNxM](https://www.axfc.net/u/3792235)、[ComittoNxT](https://www.axfc.net/u/3978158)、[ComittoNxA](https://github.com/ComittoNxA/ComittoNxA/tree/1.65A20)、[ComittoNxAC](https://www.axfc.net/u/4059552)、[ComittoNxD](https://github.com/Kdroidwin/cnxd/tree/cnxd) のソースを元にしています.  
