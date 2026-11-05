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
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.event.SimpleConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.WritableString;

/**
 * スレッドセーフに読み書きでき, 値の変更を監視できる {@link String} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の文字列としてシリアライズ/デシリアライズされる.
 * JSON の {@code null} は {@link ObservableAtomicString} の {@code null} として読み込まれる.
 *
 * <p>値の等値比較は {@link Objects#equals} で行うため, 内容が等しい別のインスタンスへ変更しても
 * イベントハンドラは呼ばれない.
 *
 * <p>値を変更するメソッドは, このオブジェクトのロックを保持したまま {@link ValueChangeEvent} の
 * イベントハンドラおよび {@code updateFunction} を呼び出す. <br>
 * このため, これらの処理の中で別のロックを取得した場合, ロックの獲得順序によってはデッドロックが発生する.
 * また, これらの処理が終わるまで, 他のスレッドはこのオブジェクトの値を読み書きできない.
 *
 * <p>イベントハンドラの中でこのオブジェクトの値を変更した場合, その変更に対するイベントハンドラが
 * 全て呼び出されてから, 元の変更に対する残りのイベントハンドラが呼び出される. <br>
 * このため, 後に呼び出されるイベントハンドラは, 既に古くなった変更内容を受け取ることがある.
 *
 * @author K.Koike
 */
@JsonAdapter(ObservableAtomicString.Adapter.class)
public class ObservableAtomicString implements WritableString {

  private String val;
  private final ConsumerInvoker<ValueChangeEvent> invoker = new SimpleConsumerInvoker<>();

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ObservableAtomicString(String val) {
    this.val = val;
  }

  @Override
  public synchronized String get() {
    return val;
  }

  @Override
  public synchronized void set(String val) {
    String oldVal = this.val;
    this.val = val;
    notifyIfChanged(oldVal, val);
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * <p>{@code expect} との比較は {@link String#equals} で行われる.
   * {@code expect} が {@code null} の場合は, このオブジェクトが保持する値が {@code null} のときに限り更新する.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public synchronized boolean compareAndSet(String expect, String update) {
    if (Objects.equals(val, expect)) {
      String oldVal = val;
      val = update;
      notifyIfChanged(oldVal, update);
      return true;
    }
    return false;
  }

  /**
   * このオブジェクトが保持する値と {@code x} に {@code accumulatorFunction} を適用した結果を,
   * このオブジェクトが保持する値にする.
   *
   * @param x {@code accumulatorFunction} の第 2 引数に渡す値
   * @param accumulatorFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public String accumulateAndGet(String x, BinaryOperator<String> accumulatorFunction) {
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
  public String getAndAccumulate(String x, BinaryOperator<String> accumulatorFunction) {
    return getAndUpdate(cur -> accumulatorFunction.apply(cur, x));
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public synchronized String getAndSet(String newValue) {
    String oldVal = val;
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
  public synchronized String getAndUpdate(UnaryOperator<String> updateFunction) {
    String oldVal = val;
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
  public synchronized String updateAndGet(UnaryOperator<String> updateFunction) {
    String oldVal = val;
    val = updateFunction.apply(val);
    notifyIfChanged(oldVal, val);
    return val;
  }

  /**
   * このオブジェクトの値が変わったときのイベントハンドラを追加する.
   *
   * @param handler 追加するイベントハンドラ
   */
  public synchronized void addOnValueChanged(Consumer<? super ValueChangeEvent> handler) {
    invoker.getRegistry().add(handler);
  }

  /**
   * このオブジェクトの値が変わったときのイベントハンドラを削除する.
   *
   * @param handler 削除するイベントハンドラ
   */
  public synchronized void removeOnValueChanged(Object handler) {
    invoker.getRegistry().remove(handler);
  }

  /**
   * {@code oldVal} と {@code newVal} が異なる場合に限り, 値が変わったときのイベントハンドラを呼び出す.
   *
   * <p>等値比較は {@link Objects#equals} で行う.
   *
   * @param oldVal 更新前の値
   * @param newVal 更新後の値
   */
  private void notifyIfChanged(String oldVal, String newVal) {
    if (!Objects.equals(oldVal, newVal)) {
      invoker.invoke(new ValueChangeEvent(this, oldVal, newVal));
    }
  }

  /**
   * {@link ObservableAtomicString} の値が変わったときの情報を格納したレコード.
   *
   * @param observer 値が変わった {@link ObservableAtomicString} オブジェクト.
   * @param oldVal 変更前の値
   * @param newVal 変更後の値
   */
  public record ValueChangeEvent(
      ObservableAtomicString observer, String oldVal, String newVal) {}

  /** {@link ObservableAtomicString} を JSON の文字列として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<ObservableAtomicString> {

    @Override
    public void write(JsonWriter out, ObservableAtomicString value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.get());
    }

    @Override
    public ObservableAtomicString read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ObservableAtomicString(in.nextString());
    }
  }
}
