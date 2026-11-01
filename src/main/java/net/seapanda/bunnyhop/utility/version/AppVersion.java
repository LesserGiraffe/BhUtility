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

package net.seapanda.bunnyhop.utility.version;


import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * アプリケーションのバージョン.
 *
 * @author K.Koike
 */
@JsonAdapter(AppVersion.Adapter.class)
public final class AppVersion extends PrefixedVersion {

  /** アプリケーションのバージョンが存在しないことを表すオブジェクト. */
  public static final AppVersion NONE = new AppVersion();

  /**
   * {@link AppVersion} オブジェクトを作成する.
   *
   * @param id 識別子名
   * @return {@link AppVersion} オブジェクト
   */
  public static AppVersion of(String id) {
    return new AppVersion(id);
  }

  /**
   * コンストラクタ.
   *
   * @param version 識別子名
   */
  private AppVersion(String version) {
    super(version);
  }

  /** デフォルトコンストラクタ. (デシリアライズ用) */
  public AppVersion() {
    super();
  }

  /** {@link AppVersion} を JSON の文字列として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<AppVersion> {

    @Override
    public void write(JsonWriter out, AppVersion value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.toString());
    }

    @Override
    public AppVersion read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String version = in.nextString();
      if (version.equals(NONE.version)) {
        return NONE;
      }
      try {
        return AppVersion.of(version);
      } catch (IllegalArgumentException e) {
        throw new JsonSyntaxException(e);
      }
    }
  }
}
