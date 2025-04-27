package net.seapanda.bunnyhop.utility.textdb;

import java.util.ArrayList;
import java.util.List;

/**
 * テキストデータの ID.
 *
 * @author K.Koike
 */
public class TextId {

  /** テキストデータの ID が存在しないことを表すオブジェクト. */
  public static final TextId NONE = TextId.of();

  private final List<String> path;

  private TextId(List<String> path) {
    this.path = (path == null) ? new ArrayList<>() : new ArrayList<>(path);
  }

  private TextId(String... path) {
    if (path == null) {
      path = new String[] {};
    }
    this.path = new ArrayList<>(List.of(path));
  }

  /**
   * {@link TextId} を作成する.
   *
   * @param path このリストの要素を JSON key として JSON のトップオブジェクトから順に JSON value を参照したときの
   *             最後の JSON value に対応する ID が作成される.
   * @return {@link TextId} オブジェクト.
   */
  public static TextId of(List<String> path) {
    return new TextId(path);
  }

  /**
   * {@link TextId} を作成する.
   *
   * @param path この配列の要素を JSON key として JSON のトップオブジェクトから順に JSON value を参照したときの
   *             最後の JSON value に対応する ID が作成される.
   * @return {@link TextId} オブジェクト.
   */
  public static TextId of(String... path) {
    return new TextId(path);
  }

  @Override
  public String toString() {
    return path.stream().reduce("", (a, b) -> "%s.%s".formatted(a, b));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TextId other) {
      return path.equals(other.path);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
