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

package net.seapanda.bunnyhop.utility.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * イミュータブルな循環リスト.
 *
 * @author K.Koike
 */
public class ImmutableCircularList<T> {

  /** 現在参照している要素のインデックスが不正であることを示す値. */
  public static int INVALID_POINTER = -1;

  private final Object[] items;
  private int pointer = INVALID_POINTER;

  /** コンストラクタ. */
  public ImmutableCircularList() {
    this.items = new Object[0];
  }

  /**
   * コンストラクタ.
   *
   * @param items この循環リストが持つ要素
   */
  public ImmutableCircularList(Collection<T> items) {
    Objects.requireNonNull(items);
    this.items = items.toArray(new Object[0]);
    this.pointer = items.isEmpty() ? INVALID_POINTER : 0;
  }

  /**
   * コンストラクタ.
   *
   * @param items この循環リストが持つ要素
   * @param pointer このオブジェクトが最初に参照する要素のインデックス.
   * @throws IndexOutOfBoundsException {@code pointer} が不正な場合
   */
  public ImmutableCircularList(Collection<T> items, int pointer) {
    Objects.requireNonNull(items);
    Objects.checkIndex(pointer, items.size());
    this.items = items.toArray(new Object[0]);
    this.pointer = pointer;
  }

  /**
   * コンストラクタ.
   *
   * @param items この循環リストが持つ要素
   */
  public ImmutableCircularList(T[] items) {
    Objects.requireNonNull(items);
    this.items = new Object[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    this.pointer = items.length == 0 ? INVALID_POINTER : 0;
  }

  /**
   * コンストラクタ.
   *
   * @param items この循環リストが持つ要素
   * @param pointer このオブジェクトが最初に参照する要素のインデックス
   * @throws IndexOutOfBoundsException {@code pointer} が不正な場合
   */
  public ImmutableCircularList(T[] items, int pointer) {
    Objects.requireNonNull(items);
    Objects.checkIndex(pointer, items.length);
    this.items = new Object[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    this.pointer = pointer;
  }

  /**
   * このオブジェクトが保持する {@code idx} 番目の要素を返す.
   *
   * @param idx この位置の要素を返す.
   * @return {@code idx} で指定された位置の要素.
   * @throws ArrayIndexOutOfBoundsException {@code idx} が不正な場合.
   */
  @SuppressWarnings("unchecked")
  public T get(int idx) {
    return (T) items[idx];
  }

  /**
   * このオブジェクトが現在参照している要素を返す.
   *
   * @return このオブジェクトが現在参照している要素.
   *         このオブジェクトが要素を 1 つも持たない場合は null を返す.
   */
  @SuppressWarnings("unchecked")
  public T getCurrent() {
    return pointer == INVALID_POINTER ? null : (T) items[pointer];
  }

  /**
   * このオブジェクトが参照する要素を現在参照している要素から 1 つ先の要素に変更し, 新しく参照した要素を返す.
   *
   * @return このオブジェクトが現在参照している要素から 1 つ先の要素.
   *         このオブジェクトが要素を 1 つも持たない場合は null を返す.
   */
  public T getNext() {
    return getAheadImpl(1);
  }

  /**
   * このオブジェクトが参照する要素を現在参照している要素から 1 つ前の要素に変更し, 新しく参照した要素を返す.
   *
   * @return このオブジェクトが現在参照している要素から 1 つ前の要素.
   *         このオブジェクトが要素を 1 つも持たない場合は null を返す.
   */
  public T getPrevious() {
    return getAheadImpl(-1);
  }

  /**
   * このオブジェクトが参照する要素を現在参照している要素から {@code step} 分先の要素に変更し, 新しく参照した要素を返す.
   *
   * @return このオブジェクトが現在参照している要素から {@code step} 分先の要素.
   *         このオブジェクトが要素を 1 つも持たない場合は null を返す.
   */
  public T getAhead(int step) {
    return getAheadImpl(step);
  }

  /**
   * このオブジェクトが参照する要素を現在参照している要素から {@code step} 分前の要素に変更し, 新しく参照した要素を返す.
   *
   * @return このオブジェクトが現在参照している要素から {@code step} 分前の要素.
   *         このオブジェクトが要素を 1 つも持たない場合は null を返す.
   */
  public T getBehind(int step) {
    return getAheadImpl(-(long) step);
  }

  @SuppressWarnings("unchecked")
  private T getAheadImpl(long step) {
    pointer = calcNextPointer(step);
    return pointer == INVALID_POINTER ? null : (T) items[pointer];
  }

  private int calcNextPointer(long diff) {
    if (items.length == 0) {
      return INVALID_POINTER;
    }
    long pointerL = (diff + (long) pointer) % (long) items.length;
    return (int) (pointerL < 0L ? (pointerL + items.length) : pointerL);
  }

  /**
   * このオブジェクトが保持している要素の数を返す.
   *
   * @return このオブジェクトが保持している要素の数
   */
  public int size() {
    return items.length;
  }

  /**
   * このオブジェクトが現在参照している要素のインデックスを返す.
   *
   * @return このオブジェクトが現在参照している要素のインデックス.
   *         このオブジェクトが要素を 1 つも持たない場合は {@link #INVALID_POINTER} 返す.
   */
  public int getPointer() {
    return pointer;
  }

  /**
   * このオブジェクトが保持している要素を返す.
   *
   * @return このオブジェクトが保持している要素のリスト
   */
  @SuppressWarnings("unchecked")
  public List<T> getItems() {
    return (List<T>) Arrays.asList(items);
  }

  /**
   * このオブジェクトが現在参照している要素のインデックスを {@code step} 分先に進める.
   *
   * @param step 進める数
   * @return 進めた後のインデックス
   */
  public int moveAhead(int step) {
    return calcNextPointer(step);
  }

  /**
   * このオブジェクトが現在参照している要素のインデックスを {@code step} 分前に戻す.
   *
   * @param step 戻す数
   * @return 戻した後のインデックス
   */
  public int movePrevious(int step) {
    return calcNextPointer(-(long) step);
  }
}
