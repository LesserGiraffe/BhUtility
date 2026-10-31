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
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.stream.Collectors;
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

  /**
   * ログを出力する.
   *
   * @param type ログの種類を表す文字列
   * @param stream ログの出力先
   * @param msg 出力するメッセージ.  書式の適用は済んでいるものとする.  null の場合は何も出力しない.
   */
  private synchronized void output(String type, PrintStream stream, String msg) {
    if (isClosed || msg == null) {
      return;
    }
    Date date = Calendar.getInstance().getTime();
    String log = "[%s] : %s @ %s\n\n%s\n------\n".formatted(
        type,
        Utility.getMethodName(3),
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date),
        msg);
    stream.print(log);
    recorder.write(log);
  }

  @Override
  public synchronized void info(String msg, Object... params) {
    output("INFO", System.out, format(msg, params));
  }

  @Override
  public synchronized void info(String msg, Throwable e) {
    output("INFO", System.out, appendExceptionMsg(msg, e));
  }

  @Override
  public synchronized void info(Throwable e, String msg, Object... params) {
    output("INFO", System.out, appendExceptionMsg(format(msg, params), e));
  }

  @Override
  public synchronized void info(Throwable e) {
    output("INFO", System.out, (e == null) ? null : createExceptionMsg(e, 0));
  }

  @Override
  public synchronized void error(String msg, Object... params) {
    output("ERR", System.err, format(msg, params));
  }

  @Override
  public synchronized void error(String msg, Throwable e) {
    output("ERR", System.err, appendExceptionMsg(msg, e));
  }

  @Override
  public synchronized void error(Throwable e, String msg, Object... params) {
    output("ERR", System.err, appendExceptionMsg(format(msg, params), e));
  }

  @Override
  public synchronized void error(Throwable e) {
    output("ERR", System.err, (e == null) ? null : createExceptionMsg(e, 0));
  }

  /**
   * {@code msg} に {@code params} を適用した文字列を作成する.
   *
   * <p>{@code params} が空の場合, {@code msg} は書式文字列として扱わずそのまま返す.
   * {@code msg} の書式指定子と {@code params} が対応しない場合は, 書式を適用せずに
   * {@code msg} と {@code params} を並べた文字列を返す.  (ログ出力で例外を発生させないため.)
   *
   * @param msg 書式文字列
   * @param params {@code msg} の書式指定子に渡す引数
   * @return 作成した文字列.  {@code msg} が null の場合は null.
   */
  private static String format(String msg, Object... params) {
    if (msg == null || params == null || params.length == 0) {
      return msg;
    }
    try {
      return msg.formatted(params);
    } catch (IllegalFormatException e) {
      return msg + Arrays.stream(params)
          .map(String::valueOf)
          .collect(Collectors.joining(", ", "\n  params: [", "]"));
    }
  }

  /**
   * {@code msg} の後ろに {@code e} の情報を並べた文字列を作成する.
   *
   * @param msg 例外の情報を追加するメッセージ.  書式の適用は済んでいるものとする.
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を追加する.
   *          null の場合は {@code msg} をそのまま返す.
   * @return 作成した文字列.  {@code msg} が null の場合は null.
   */
  private static String appendExceptionMsg(String msg, Throwable e) {
    String errMsg = createExceptionMsg(e, 2);
    if (msg == null || errMsg.isEmpty()) {
      return msg;
    }
    return msg + "\n\n" + errMsg;
  }

  /**
   * 例外とその原因となった例外の情報を並べたログ出力用のメッセージを作成する.
   *
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報をメッセージに含める
   * @param numSpaces メッセージの先頭に入れるスペースの数
   * @return 作成したメッセージ.
   */
  private static String createExceptionMsg(Throwable e, int numSpaces) {
    if (e == null) {
      return "";
    }
    var msg = new StringBuilder();
    Throwable cause = e;
    do {
      msg.repeat(" ", numSpaces)
          .append(cause.getClass().getName())
          .append(":\n")
          .append(indent(String.valueOf(cause.getMessage()), numSpaces + 2));
      cause = cause.getCause();
    } while (cause != null);
    return msg.toString();
  }

  /** {@code numSpaces} で指定した分だけインデントした {@code text} を文字列として返す. */
  private static String indent(String text, int numSpaces) {
    String space = " ".repeat(numSpaces);
    return Arrays.stream(text.split("\n"))
        .map(line -> space + line)
        .collect(Collectors.joining("\n"));
  }

  /** 終了処理をする. */
  public synchronized void close() {
    isClosed = true;
    recorder.close();
  }  
}
