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

package net.seapanda.bunnyhop.utility.textdb;

import java.util.List;

/**
 * テキストデータを取得する機能を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface TextDatabase {

  /**
   * テキストを取得する.
   *
   * @param id 取得するテキストの ID
   * @param objs 空でない場合, {@code path} で指定したテキストデータに {@link String#format} を適用し,
   *             その引数にこれらのオブジェクトを渡す.
   * @return {@code id} と {@code objs} から作成された文字列.
   *         {@code id} に対応するテキストデータが見つからない場合は, 空の文字列を返す.
   *         {@code objs} によるフォーマットに失敗した場合は,  空の文字列を返す.
   */
  String get(TextId id, Object... objs);

  /**
   * テキストを取得する.
   *
   * @param path このリストで特定されるテキストデータを返す
   * 
   * @param objs 空でない場合, {@code path} で指定したテキストデータに {@link String#format} を適用し,
   *             その引数にこれらのオブジェクトを渡す.
   * @return {@code path} と {@code objs} から作成された文字列.
   *         {@code path} に対応するテキストデータが見つからない場合は, 空の文字列を返す.
   *         {@code objs} によるフォーマットに失敗した場合は,  空の文字列を返す.
   */
  String get(List<String> path, Object... objs);

  /**
   * テキストを取得する.
   *
   * @param path このリストで特定されるテキストデータを返す
   * @return {@code path} で特定されたテキストデータ
   */
  public String get(String... path);
}
