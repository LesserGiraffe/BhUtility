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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * ファイルにテキストを出力するクラス.
 *
 * @author K.Koike
 */
public class TextRecorder implements Closeable {

  /** 出力先ファイルの出力ストリーム. */
  private OutputStream os;
  /** 出力先ファイルを作成するディレクトリのパス. */
  private final Path dirPath;
  /** 出力先ファイルの共通部分の名前. */
  private final String fileName;
  /** 出力先ファイルの最大サイズ. (Bytes)   このサイズを超えると新しいファイルを作成する. */
  private final int maxFileSize;
  /** 出力先ファイルの最大個数. この個数を超えるとファイルのローテーションを行う. */
  private final int maxFiles;
  /** 現在書き込んでいるファイルのサイズ. (Bytes) */
  private long currentFileSize = 0;
  /** 現在書き込んでいるファイルのパス. */
  private final Path logFilePath;

  /**
   * コンストラクタ.
   *
   * @param dirPath 出力先ファイルを作成するディレクトリのパス.
   * @param fileName 出力先ファイルの共通部分の名前.
   * @param maxFileSize 出力先ファイルの最大サイズ (Bytes)
   * @param maxFiles 出力先ファイルの最大個数. この個数を超えるとファイルのローテーションを行う. (1 ~ 9999)
   */
  TextRecorder(Path dirPath, String fileName, int maxFileSize, int maxFiles)
      throws IOException {
    if (maxFiles < 1 || 9999 < maxFiles) {
      throw new IllegalArgumentException(String.format(
          "'maxFile' must be between %s and %s inclusive.\n%s was set.", 1, 9999, maxFiles));
    }
    this.dirPath = dirPath;
    this.fileName = fileName;
    this.maxFileSize = maxFileSize;
    this.maxFiles = maxFiles;
    this.logFilePath = genLogFilePath(0);
    initialize();
  }

  private void initialize() throws IOException {
    if (!Files.isDirectory(dirPath)) {
      Files.createDirectory(dirPath);
    }
    if (!Files.exists(logFilePath)) {
      Files.createFile(logFilePath);
    }
    // ファイルのローテーション
    if (Files.size(logFilePath) > maxFileSize) {
      renameLogFiles();
    }
    currentFileSize = Files.size(logFilePath);
    os = Files.newOutputStream(
        logFilePath,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
  }

  /**
   * 出力先ファイルにテキストを書き込む.
   *
   * @param msg 出力先ファイルに書き込むテキスト
   */
  public void write(String msg) {
    try {
      if (os != null) {
        byte[] msgData = msg.getBytes(StandardCharsets.UTF_8);
        if ((currentFileSize + msgData.length) > maxFileSize) {
          reopen();
        }
        os.write(msgData);
        os.flush();
      }
    } catch (IOException | SecurityException e) { /* do nothing */ }
  }

  /** ファイルローテーション後に新しくファイルを開く. */
  private void reopen() throws IOException {
    os.close();
    renameLogFiles();
    currentFileSize = Files.size(logFilePath);
    os = Files.newOutputStream(
        logFilePath,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
  }

  /**
   * ファイルをローテンションするため, ファイルをリネームする.
   */
  private void renameLogFiles() throws IOException {
    Path oldestLogFilePath = genLogFilePath(maxFiles - 1);
    if (Files.exists(oldestLogFilePath)) {
      Files.delete(oldestLogFilePath);
    }
    for (int fileNo = maxFiles - 2; fileNo >= 0; --fileNo) {
      Path oldLogFilePath = genLogFilePath(fileNo);
      Path newLogFilePath = genLogFilePath(fileNo + 1);
      if (Files.exists(oldLogFilePath)) {
        Files.move(oldLogFilePath, newLogFilePath, StandardCopyOption.ATOMIC_MOVE);
      }
    }
  }

  private Path genLogFilePath(int fileNo) {
    String numStr = ("0000" + fileNo);
    numStr = numStr.substring(numStr.length() - 4);
    String fileName = "%s-%s.log".formatted(this.fileName, numStr);
    return dirPath.resolve(fileName);
  }

  /** 終了処理をする. */
  @Override
  public void close() {
    try {
      if (os != null) {
        os.close();
      }
    } catch (IOException e) { /* do nothing */ }
  }
}
