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

package net.seapanda.bunnyhop.utility;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 各種バージョンを表すクラスの基底クラス.
 *
 * @author K.Koike
 */
public abstract class Version implements Serializable {

  public final String version;
  public final String prefix;
  public final String major;
  public final String minor;
  public final String patch;

  /**
   * コンストラクタ.
   *
   * @param version 識別子名 (例 : bh-2.1.6)
   */
  protected Version(String version) {
    this.version = version;
    if (version.isEmpty()) {
      prefix = "";
      major = "";
      minor = "";
      patch = "";
      return;
    }
    Matcher matcher =
        Pattern.compile("([a-zA-Z0-9]+)\\-(\\d+)\\.(\\d+)\\.(\\d+)").matcher(version);
    if (version == null || !matcher.find()) {
      throw new IllegalArgumentException("Invalid BunnyHop version format (%s)".formatted(version));
    }
    prefix = matcher.group(1);
    major = matcher.group(2);
    minor = matcher.group(3);
    patch = matcher.group(4);
  }

  /** 接頭語部分を比較する. */
  public boolean comparePrefix(Version other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return other.prefix.equals(prefix);
  }

  /** メジャー番号を比較する. */
  public boolean compareMajor(Version other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return other.major.equals(major);
  }

  /** マイナー番号を比較する. */
  public boolean compareMinor(Version other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return other.minor.equals(minor);
  }

  /** パッチ番号を比較する. */
  public boolean comparePatch(Version other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return other.patch.equals(patch);
  }

  @Override
  public String toString() {
    return version;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return version.equals(((Version) other).version);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(version);
  }  
}
