/*
 * Copyright 2017 K.Koike
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.seapanda.bunnyhop.utility.log;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * ファイルにログを出力する機能を提供するクラス.
 *
 * @author K.Koike
 */
public class FileLogger implements Logger {

  private final TextRecorder recorder;
  private boolean isClosed = false;
  
  /**
   * コンストラクタ.
   *
   * @param dirPath 出力先ファイルを作成するディレクトリのパス.
   * @param fileName 出力先ファイルの共通部分の名前.
   * @param maxFileSize 出力先ファイルの最大サイズ (Bytes)
   * @param maxFiles 出力先ファイルの最大個数. この個数を超えるとファイルのローテーションを行う. (1 ~ 9999)
   */
  public FileLogger(Path dirPath, String fileName, int maxFileSize, int maxFiles)
      throws IOException {
    recorder = new TextRecorder(dirPath, fileName, maxFileSize, maxFiles);
  }

  /** デバッグ用エラーメッセージ出力メソッド. */
  public synchronized void error(String msg) {
    if (isClosed) {
      return;
    }
    Date date = Calendar.getInstance().getTime();
    msg = "[ERR] : %s @ %s\n%s\n----\n".formatted(
        Utility.getMethodName(2),
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date),
        msg);
    System.err.print(msg);
    recorder.write(msg);
  }

  /** デバッグ用メッセージ出力メソッド. */
  public synchronized void info(String msg) {
    if (isClosed) {
      return;
    }
    Date date = Calendar.getInstance().getTime();
    msg = "[INFO] : %s @ %s\n%s\n----\n".formatted(
        Utility.getMethodName(2),
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date),
        msg);
    System.out.print(msg);
    recorder.write(msg);
  }

  /** 終了処理をする. */
  public synchronized void close() {
    isClosed = true;
    recorder.close();
  }  
}
