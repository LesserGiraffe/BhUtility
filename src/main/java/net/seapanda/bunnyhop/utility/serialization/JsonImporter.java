package net.seapanda.bunnyhop.utility.serialization;

import static net.seapanda.bunnyhop.utility.function.ThrowingConsumer.unchecked;

import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * JSON ファイルを読み込み, 指定したクラスの static フィールドに反映するユーティリティクラス.
 *
 * @author K.Koike
 */
public class JsonImporter {

  /**
   * JSON ファイルから設定を読み込み {@code target} の static 変数に格納する.
   *
   * @param filePath 読み込む JSON ファイルパス
   * @throws IOException ファイル読み込みに失敗した場合
   */
  public static void imports(Class<?> target, Path filePath) throws IOException {
    Gson gson = new Gson();
    try (var reader = Files.newBufferedReader(filePath)) {
      @SuppressWarnings("unchecked")
      Map<String, Object> settingsMap = gson.fromJson(reader, Map.class);
      applyFieldsToClass(target, settingsMap);
    }
  }

  /** Map の内容を指定クラスの static フィールドに再帰的に適用する. */
  private static void applyFieldsToClass(Class<?> clazz, Map<String, Object> map) {
    if (map == null) {
      return;
    }

    for (Field field : clazz.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
        continue;
      }
      try {
        field.setAccessible(true);
        readValue(field.getName(), field.getType(), map)
            .ifPresent(unchecked(val -> field.set(null, val)));
      } catch (Exception e) { /* Do Nothing */ }
    }

    // 内部クラスを再帰的に処理
    for (Class<?> innerClass : clazz.getDeclaredClasses()) {
      String className = innerClass.getSimpleName();
      if (map.containsKey(className) && map.get(className) instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> innerMap = (Map<String, Object>) map.get(className);
        applyFieldsToClass(innerClass, innerMap);
      }
    }
  }

  /** {@code name} と {@code type} で指定したフィールドの値を {@code map} から読みだして格納する. */
  private static Optional<Object> readValue(String name, Class<?> type, Map<String, Object> map)
      throws ReflectiveOperationException {
    if (!map.containsKey(name)) {
      return Optional.empty();
    }
    return Optional.ofNullable(convertValue(map.get(name), type));
  }

  /** JSON から読み込んだ値を適切な型に変換する. */
  private static Object convertValue(Object value, Class<?> targetType)
      throws ReflectiveOperationException {
    if (value == null) {
      return null;
    }

    // enum 型の場合は of メソッドを使用
    if (targetType.isEnum()) {
      Method ofMethod = targetType.getMethod("valueOf", String.class);
      return ofMethod.invoke(null, value.toString());
    }

    // プリミティブ型およびラッパー型の変換
    if (targetType == int.class || targetType == Integer.class) {
      return ((Number) value).intValue();
    }
    if (targetType == long.class || targetType == Long.class) {
      return ((Number) value).longValue();
    }
    if (targetType == double.class || targetType == Double.class) {
      return ((Number) value).doubleValue();
    }
    if (targetType == float.class || targetType == Float.class) {
      return ((Number) value).floatValue();
    }
    if (targetType == boolean.class || targetType == Boolean.class) {
      return value;
    }
    if (targetType == String.class) {
      return value.toString();
    }

    // 一般的なクラスの場合は Gson を使って変換
    if (value instanceof Map) {
      Gson gson = new Gson();
      String json = gson.toJson(value);
      return gson.fromJson(json, targetType);
    }

    return value;
  }
}
