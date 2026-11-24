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

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.event.SimpleConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.ObservableValue;

/**
 * スレッドセーフに読み書きでき, 値の変更を監視できる値.
 *
 * <p>値の等値比較は参照の同一性 ({@code ==}) で行うため, 内容が等しい別のインスタンスへ変更した場合も
 * イベントハンドラが呼ばれる.
 *
 * <p>値を変更するメソッドは, このオブジェクトのロックを保持したまま {@link ValueChangeEvent} の
 * イベントハンドラおよび {@code updateFunction} を呼び出す. <br>
 * このため, これらの処理の中で別のロックを取得した場合, ロックの獲得順序によってはデッドロックが発生する.
 * また, これらの処理が終わるまで, 他のスレッドはこのオブジェクトの値を読み書きできない.
 *
 * <p>イベントハンドラの中でこのオブジェクトの値を変更した場合, 現在の変更に対するイベントハンドラを
 * 全て呼び出した後に, その新しい変更に対するイベントハンドラが呼び出される. <br>
 * このため, 各イベントハンドラは全ての変更を発生順に受け取る. <br>
 * ただし, イベントハンドラが受け取る変更内容と, その中で {@link #get} した値は一致しないことがある.
 *
 * @param <T> このオブジェクトが保持する値の型
 * @author K.Koike
 */
public class ObservableAtomicValue<T> implements ObservableValue<T> {

  private T val;
  private final ConsumerInvoker<ValueChangeEvent<T>> invoker = new SimpleConsumerInvoker<>();

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ObservableAtomicValue(T val) {
    this.val = val;
  }

  @Override
  public synchronized T get() {
    return val;
  }

  @Override
  public synchronized void set(T val) {
    T oldVal = this.val;
    this.val = val;
    notifyIfChanged(oldVal, val);
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
    return updateAndGet(cur -> accumulatorFunction.apply(cur, x));
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
    return getAndUpdate(cur -> accumulatorFunction.apply(cur, x));
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
  public synchronized boolean compareAndSet(T expect, T update) {
    if (val == expect) {
      val = update;
      notifyIfChanged(expect, update);
      return true;
    }
    return false;
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public synchronized T getAndSet(T newValue) {
    T oldVal = val;
    this.val = newValue;
    notifyIfChanged(oldVal, newValue);
    return oldVal;
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public synchronized T getAndUpdate(UnaryOperator<T> updateFunction) {
    T oldVal = val;
    val = updateFunction.apply(val);
    notifyIfChanged(oldVal, val);
    return oldVal;
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public synchronized T updateAndGet(UnaryOperator<T> updateFunction) {
    T oldVal = val;
    val = updateFunction.apply(val);
    notifyIfChanged(oldVal, val);
    return val;
  }

  @Override
  public synchronized void addOnValueChanged(Consumer<? super ValueChangeEvent<T>> handler) {
    invoker.getRegistry().add(handler);
  }

  @Override
  public synchronized void removeOnValueChanged(Object handler) {
    invoker.getRegistry().remove(handler);
  }

  /**
   * {@code oldVal} と {@code newVal} が異なる場合に限り, 値が変わったときのイベントハンドラを呼び出す.
   *
   * <p>等値比較は参照の同一性 ({@code ==}) で行う.
   *
   * @param oldVal 更新前の値
   * @param newVal 更新後の値
   */
  private void notifyIfChanged(T oldVal, T newVal) {
    if (oldVal != newVal) {
      invoker.invoke(new ValueChangeEvent<>(this, oldVal, newVal));
    }
  }
}
