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

package net.seapanda.bunnyhop.utility.concurrent.reference;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import net.seapanda.bunnyhop.utility.reference.WritableValue;

/**
 * スレッドセーフに読み書きできる値.
 *
 * @param <T> このオブジェクトが保持する値の型
 * @author K.Koike
 */
public class ConcurrentValue<T> implements WritableValue<T> {

  private final AtomicReference<T> val;

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ConcurrentValue(T val) {
    this.val = new AtomicReference<>(val);
  }

  @Override
  public T get() {
    return val.get();
  }

  @Override
  public void set(T val) {
    this.val.set(val);
  }

  /**
   * このオブジェクトが保持する値と {@code x} に {@code accumulatorFunction} を適用した結果を,
   * このオブジェクトが保持する値にする.
   *
   * @param x {@code accumulatorFunction} の第 2 引数に渡す値
   * @param accumulatorFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public T accumulateAndGet(T x, BinaryOperator<T> accumulatorFunction) {
    return val.accumulateAndGet(x, accumulatorFunction);
  }

  /**
   * このオブジェクトが保持する値と {@code x} に {@code accumulatorFunction} を適用した結果を,
   * このオブジェクトが保持する値にする.
   *
   * @param x {@code accumulatorFunction} の第 2 引数に渡す値
   * @param accumulatorFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public T getAndAccumulate(T x, BinaryOperator<T> accumulatorFunction) {
    return val.getAndAccumulate(x, accumulatorFunction);
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * <p>{@code expect} との比較は参照の同一性 ({@code ==}) で行われる.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public boolean compareAndSet(T expect, T update) {
    return val.compareAndSet(expect, update);
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public T getAndSet(T newValue) {
    return val.getAndSet(newValue);
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public T getAndUpdate(UnaryOperator<T> updateFunction) {
    return val.getAndUpdate(updateFunction);
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public T updateAndGet(UnaryOperator<T> updateFunction) {
    return val.updateAndGet(updateFunction);
  }
}
