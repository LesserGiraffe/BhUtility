/*
 * Copyright 2017 K.Koike
 *
 * Licensed under the Apache License, PrefixedVersion 2.0 (the "License");
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

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 接頭語付きバージョンを表すクラス.
 *
 * @author K.Koike
 */
public class PrefixedVersion implements Serializable {

  private static final Pattern pattern =
      Pattern.compile("([a-zA-Z0-9_]+)-([1-9]\\d*|0)\\.([1-9]\\d*|0)\\.([1-9]\\d*|0)");

  public final String version;
  public final String prefix;
  public final int major;
  public final int minor;
  public final int patch;

  /** デフォルトコンストラクタ. (デシリアライズ用) */
  public PrefixedVersion() {
    version = "";
    prefix = "";
    major = Integer.MIN_VALUE;
    minor = Integer.MIN_VALUE;
    patch = Integer.MIN_VALUE;
  }

  /**
   * コンストラクタ.
   *
   * @param version 識別子名 (例 : bh-2.1.6)
   */
  public PrefixedVersion(String version) {
    if (version == null) {
      throw new IllegalArgumentException("Invalid version format (null)");
    }
    Matcher matcher = pattern.matcher(version);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid version format (%s)".formatted(version));
    }
    this.version = version;
    prefix = matcher.group(1);
    major = Integer.parseInt(matcher.group(2));
    minor = Integer.parseInt(matcher.group(3));
    patch = Integer.parseInt(matcher.group(4));
  }

  /**
   * 接頭語部分を比較する.
   *
   * @return このバージョンの接頭語が指定されたバージョンの接頭語より小さい場合は負の整数,
   *         等しい場合はゼロ, 大きい場合は正の整数.
   */
  public int comparePrefix(PrefixedVersion other) {
    if (other == null) {
      throw new NullPointerException("Cannot read field \"prefix\" because \"other\" is null");
    }
    return prefix.compareTo(other.prefix);
  }

  /**
   * メジャー番号を比較する.
   *
   * @return このバージョンのメジャー番号が指定されたバージョンのメジャー番号より小さい場合は負の整数,
   *         等しい場合はゼロ, 大きい場合は正の整数.
   */
  public int compareMajor(PrefixedVersion other) {
    if (other == null) {
      throw new NullPointerException("Cannot read field \"major\" because \"other\" is null");
    }
    return Integer.compare(major, other.major);
  }

  /**
   * マイナー番号を比較する.
   *
   * @return このバージョンのマイナー番号が指定されたバージョンのマイナー番号より小さい場合は負の整数,
   *         等しい場合はゼロ, 大きい場合は正の整数.
   */
  public int compareMinor(PrefixedVersion other) {
    if (other == null) {
      throw new NullPointerException("Cannot read field \"minor\" because \"other\" is null");
    }
    return Integer.compare(minor, other.minor);
  }

  /**
   * パッチ番号を比較する.
   *
   * @return このバージョンのパッチ番号が指定されたバージョンのパッチ番号より小さい場合は負の整数,
   *         等しい場合はゼロ, 大きい場合は正の整数.
   */
  public int comparePatch(PrefixedVersion other) {
    if (other == null) {
      throw new NullPointerException("Cannot read field \"patch\" because \"other\" is null");
    }
    return Integer.compare(patch, other.patch);
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
    return version.equals(((PrefixedVersion) other).version);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(version);
  }
}
