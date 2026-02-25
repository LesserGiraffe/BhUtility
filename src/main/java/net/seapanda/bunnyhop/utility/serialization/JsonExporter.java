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

package net.seapanda.bunnyhop.utility.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指定したクラスの static フィールドを JSON ファイルに書き出すユーティリティクラス.
 *
 * @author K.Koike
 */
public class JsonExporter {

  /**
   * {@code target} の全 static フィールドを JSON ファイルに書き出す.
   * 内部クラスの深さに関係なく再帰的に処理する.
   *
   * @param target static フィールドを JSON ファイルに書き出すクラスのクラスオブジェクト
   * @param filePath 出力先の JSON ファイルパス
   * @throws IOException ファイル書き込みに失敗した場合
   */
  public static void export(Class<?> target, Path filePath) throws IOException {
    Map<String, Object> settingsMap = collectClassFields(target);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      gson.toJson(settingsMap, writer);
    }
  }

  /** 引数で指定したクラスの static フィールドと内部クラスを再帰的に Map に収集する. */
  private static Map<String, Object> collectClassFields(Class<?> clazz) {
    Map<String, Object> map = new LinkedHashMap<>();

    // static フィールドを収集
    for (Field field : clazz.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      if (!Modifier.isStatic(modifiers)
          || field.isSynthetic()
          || field.isAnnotationPresent(PreventExport.class)) {
        continue;
      }

      try {
        field.setAccessible(true);
        map.put(field.getName(), field.get(null));
      } catch (Exception e) { /* Do nothing. */ }
    }

    // 内部クラスを再帰的に処理
    for (Class<?> innerClass : clazz.getDeclaredClasses()) {
      Map<String, Object> innerMap = collectClassFields(innerClass);
      if (!innerMap.isEmpty()) {
        map.put(innerClass.getSimpleName(), innerMap);
      }
    }
    return map;
  }
}
