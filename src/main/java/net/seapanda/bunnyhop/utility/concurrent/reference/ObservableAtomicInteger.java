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

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.event.SimpleConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.ObservableInteger;

/**
 * スレッドセーフに読み書きでき, 値の変更を監視できる {@code int} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の数値としてシリアライズ/デシリアライズされる.
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
 * @author K.Koike
 */
@JsonAdapter(ObservableAtomicInteger.Adapter.class)
public class ObservableAtomicInteger implements ObservableInteger {

  private int val;
  private final ConsumerInvoker<ValueChangeEvent> invoker = new SimpleConsumerInvoker<>();

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ObservableAtomicInteger(int val) {
    this.val = val;
  }

  @Override
  public synchronized int get() {
    return val;
  }

  @Override
  public synchronized void set(int val) {
    int oldVal = this.val;
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
  public int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
    return updateAndGet(cur -> accumulatorFunction.applyAsInt(cur, x));
  }

  /**
   * このオブジェクトが保持する値と {@code x} に {@code accumulatorFunction} を適用した結果を,
   * このオブジェクトが保持する値にする.
   *
   * @param x {@code accumulatorFunction} の第 2 引数に渡す値
   * @param accumulatorFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
    return getAndUpdate(cur -> accumulatorFunction.applyAsInt(cur, x));
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public synchronized boolean compareAndSet(int expect, int update) {
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
  public synchronized int getAndSet(int newValue) {
    int oldVal = val;
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
  public synchronized int getAndUpdate(IntUnaryOperator updateFunction) {
    int oldVal = val;
    val = updateFunction.applyAsInt(val);
    notifyIfChanged(oldVal, val);
    return oldVal;
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public synchronized int updateAndGet(IntUnaryOperator updateFunction) {
    int oldVal = val;
    val = updateFunction.applyAsInt(val);
    notifyIfChanged(oldVal, val);
    return val;
  }

  /**
   * このオブジェクトが保持する値に {@code delta} を加算する.
   *
   * @param delta このオブジェクトが保持する値に加算する値
   * @return 加算前の値
   */
  public int getAndAdd(int delta) {
    return getAndAccumulate(delta, Integer::sum);
  }

  /**
   * このオブジェクトが保持する値に {@code delta} を加算する.
   *
   * @param delta このオブジェクトが保持する値に加算する値
   * @return 加算後の値
   */
  public int addAndGet(int delta) {
    return accumulateAndGet(delta, Integer::sum);
  }

  @Override
  public synchronized void addOnValueChanged(Consumer<? super ValueChangeEvent> handler) {
    invoker.getRegistry().add(handler);
  }

  @Override
  public synchronized void removeOnValueChanged(Object handler) {
    invoker.getRegistry().remove(handler);
  }

  /**
   * {@code oldVal} と {@code newVal} が異なる場合に限り, 値が変わったときのイベントハンドラを呼び出す.
   *
   * @param oldVal 更新前の値
   * @param newVal 更新後の値
   */
  private void notifyIfChanged(int oldVal, int newVal) {
    if (oldVal != newVal) {
      invoker.invoke(new ValueChangeEvent(this, oldVal, newVal));
    }
  }

  /** {@link ObservableAtomicInteger} を JSON の数値として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<ObservableAtomicInteger> {

    @Override
    public void write(JsonWriter out, ObservableAtomicInteger value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.get());
    }

    @Override
    public ObservableAtomicInteger read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ObservableAtomicInteger(in.nextInt());
    }
  }
}
