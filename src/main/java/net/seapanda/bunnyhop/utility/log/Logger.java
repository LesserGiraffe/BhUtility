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

/**
 * ログ出力機能を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface Logger {
  
  /**
   * 情報レベルのメッセージをログに出力する.
   *
   * @param msg 出力するメッセージ.  {@link String#format} の書式文字列として扱われる.
   *            null の場合は何も出力しない.
   * @param params {@code msg} の書式指定子に渡す引数
   */
  void info(String msg, Object... params);

  /**
   * 情報レベルのメッセージと例外の情報をログに出力する.
   *
   * @param msg 出力するメッセージ.  null の場合は何も出力しない.
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を {@code msg} の後ろに出力する.
   *          null の場合は例外の情報を出力しない.
   */
  void info(String msg, Throwable e);

  /**
   * 情報レベルのメッセージと例外の情報をログに出力する.
   *
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を {@code msg} の後ろに出力する.
   *          null の場合は例外の情報を出力しない.
   * @param msg 出力するメッセージ.  {@link String#format} の書式文字列として扱われる.
   *            null の場合は何も出力しない.
   * @param params {@code msg} の書式指定子に渡す引数
   */
  void info(Throwable e, String msg, Object... params);

  /**
   * 例外の情報を情報レベルのログに出力する.
   *
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を出力する.
   *          null の場合は何も出力しない.
   */
  void info(Throwable e);

  /**
   * エラーレベルのメッセージをログに出力する.
   *
   * @param msg 出力するメッセージ.  {@link String#format} の書式文字列として扱われる.
   *            null の場合は何も出力しない.
   * @param params {@code msg} の書式指定子に渡す引数
   */
  void error(String msg, Object... params);

  /**
   * エラーレベルのメッセージと例外の情報をログに出力する.
   *
   * @param msg 出力するメッセージ.  null の場合は何も出力しない.
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を {@code msg} の後ろに出力する.
   *          null の場合は例外の情報を出力しない.
   */
  void error(String msg, Throwable e);

  /**
   * エラーレベルのメッセージと例外の情報をログに出力する.
   *
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を {@code msg} の後ろに出力する.
   *          null の場合は例外の情報を出力しない.
   * @param msg 出力するメッセージ.  {@link String#format} の書式文字列として扱われる.
   *            null の場合は何も出力しない.
   * @param params {@code msg} の書式指定子に渡す引数
   */
  void error(Throwable e, String msg, Object... params);

  /**
   * 例外の情報をエラーレベルのログに出力する.
   *
   * @param e このオブジェクトとこのオブジェクトの原因となった例外の情報を出力する.
   *          null の場合は何も出力しない.
   */
  void error(Throwable e);
}
