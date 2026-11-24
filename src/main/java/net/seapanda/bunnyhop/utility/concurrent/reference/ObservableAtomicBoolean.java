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
import java.util.function.UnaryOperator;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.event.SimpleConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.ObservableBoolean;

/**
 * スレッドセーフに読み書きでき, 値の変更を監視できる {@code boolean} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の真偽値としてシリアライズ/デシリアライズされる.
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
@JsonAdapter(ObservableAtomicBoolean.Adapter.class)
public class ObservableAtomicBoolean implements ObservableBoolean {

  private boolean val;
  private final ConsumerInvoker<ValueChangeEvent> invoker = new SimpleConsumerInvoker<>();

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ObservableAtomicBoolean(boolean val) {
    this.val = val;
  }

  @Override
  public synchronized boolean get() {
    return val;
  }

  @Override
  public synchronized void set(boolean val) {
    boolean oldVal = this.val;
    this.val = val;
    notifyIfChanged(oldVal, val);
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public synchronized boolean compareAndSet(boolean expect, boolean update) {
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
  public synchronized boolean getAndSet(boolean newValue) {
    boolean oldVal = val;
    this.val = newValue;
    notifyIfChanged(oldVal, newValue);
    return oldVal;
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数. {@code null} を返してはいけない.
   * @return 更新前の値
   */
  public synchronized boolean getAndUpdate(UnaryOperator<Boolean> updateFunction) {
    boolean oldVal = val;
    val = updateFunction.apply(val);
    notifyIfChanged(oldVal, val);
    return oldVal;
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数. {@code null} を返してはいけない.
   * @return 更新後の値
   */
  public synchronized boolean updateAndGet(UnaryOperator<Boolean> updateFunction) {
    boolean oldVal = val;
    val = updateFunction.apply(val);
    notifyIfChanged(oldVal, val);
    return val;
  }

  /**
   * このオブジェクトが保持する値を反転する.
   *
   * @return 反転後の値
   */
  public synchronized boolean negate() {
    val = !val;
    notifyIfChanged(!val, val);
    return val;
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
  private void notifyIfChanged(boolean oldVal, boolean newVal) {
    if (oldVal != newVal) {
      invoker.invoke(new ValueChangeEvent(this, oldVal, newVal));
    }
  }

  /** {@link ObservableAtomicBoolean} を JSON の真偽値として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<ObservableAtomicBoolean> {

    @Override
    public void write(JsonWriter out, ObservableAtomicBoolean value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.get());
    }

    @Override
    public ObservableAtomicBoolean read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ObservableAtomicBoolean(in.nextBoolean());
    }
  }
}
