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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Json 形式で定義されたテキストデータを取得する機能を提供するクラス.
 *
 * @author K.Koike
 */
public class JsonTextDatabase implements TextDatabase {
  
  /** テキストのデータベース. */
  Map<TextId, String> database = new ConcurrentHashMap<>();

  /**
   * {@code filePath} で指定されたファイルからテキストデータのデータベースを構築する.
   *
   * @param filePath テキストデータが定義された JSON ファイルのパス.
   */
  public JsonTextDatabase(Path filePath) throws
      IOException,
      JsonIOException,
      JsonSyntaxException {
    var gson = new Gson();
    try (var jr = gson.newJsonReader(new FileReader(filePath.toString()))) {
      JsonObject jsonObj = gson.fromJson(jr, JsonObject.class);
      createDatabase(new LinkedList<String>(), jsonObj);
    }
  }

  /**
   * {@code jsonStr} に入力された JSON 文字列からテキストデータのデータベースを構築する.
   *
   * @param jsonStr テキストデータが定義された JSON 文字列.
   */
  public JsonTextDatabase(String jsonStr) throws JsonSyntaxException {
    var gson = new Gson();
    JsonObject jsonObj = gson.fromJson(jsonStr, JsonObject.class);
    createDatabase(new LinkedList<String>(), jsonObj);
  }

  /**
   * テキストデータを {@link #database} に格納する.
   *
   * @param keyPath トップオブジェクトから {@code value} を参照するまでに参照した key のスタック.
   *                スタックのトップが {@code value} に対応する JSON key となる.
   * @param value {@code key} に対応する JSON value
   */
  private void createDatabase(Deque<String> keyPath, JsonElement value) {
    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
      var id = TextId.of(new ArrayList<>(keyPath));
      database.put(id, value.getAsString());
      return;
    }
    if (value.isJsonObject()) {
      JsonObject jsonObj = value.getAsJsonObject();
      for (String key : jsonObj.keySet()) {
        keyPath.addLast(key);
        createDatabase(keyPath, jsonObj.get(key));
        keyPath.removeLast();
      }
    }
  }

  @Override
  public String get(TextId id, Object... objs) {
    try {
      if (!database.containsKey(id)) {
        return "";
      }
      if (objs.length == 0) {
        return database.get(id);
      }
      return database.get(id).formatted(objs);
    } catch (Exception e) {
      String objsStr = Stream.of(objs)
          .map(Object::toString)
          .reduce("", "%s, %s"::formatted);
      System.err.printf(
          ("Failed to generate a text.  id = %s, objs = %s\n%s".formatted(id, objsStr, e)) + "%n");
      return "";
    }
  }

  /**
   * テキストを取得する.
   *
   * @param path このリストの要素を JSON key として JSON のトップオブジェクトから順に JSON value を参照したときの
   *             最後の JSON value に対応するテキストデータを取得する.
   * 
   * @param objs 空でない場合, {@code path} で取得したテキストデータに {@link String#format} を適用し,
   *             その引数にこれらのオブジェクトを渡す.
   * @return {@code path} と {@code objs} から作成された文字列.
   *         {@code path} に対応するテキストデータが見つからない場合は, 空の文字列を返す.
   *         {@code objs} によるフォーマットに失敗した場合は,  空の文字列を返す.
   */
  @Override
  public String get(List<String> path, Object... objs) {
    try {
      var id = TextId.of(path);
      if (!database.containsKey(id)) {
        return "";
      }
      if (objs.length == 0) {
        return database.get(id);
      }
      return database.get(id).formatted(objs);
    } catch (Exception e) {
      String pathStr = path.stream().reduce("", "%s, %s"::formatted);
      String objsStr = Stream.of(objs)
          .map(Object::toString)
          .reduce("", "%s, %s"::formatted);
      System.err.printf(
          "Failed to generate a text.  id = %s, objs = %s\n%s%n", pathStr, objsStr, e);
      return "";
    }
  }

  /**
   * テキストを取得する.
   *
   * @param path この配列の要素を JSON key として JSON のトップオブジェクトから順に JSON value を参照したときの
   *             最後の JSON value に対応するテキストデータを取得する.
   * @return テキスト
   */
  @Override
  public String get(String... path) {
    return database.getOrDefault(TextId.of(path), "");
  }
}
