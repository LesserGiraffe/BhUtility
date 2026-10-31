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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import net.seapanda.bunnyhop.utility.reference.WritableString;

/**
 * スレッドセーフに読み書きできる {@link String} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の文字列としてシリアライズ/デシリアライズされる.
 * JSON の {@code null} は {@link ConcurrentString} の {@code null} として読み込まれる.
 *
 * @author K.Koike
 */
@JsonAdapter(ConcurrentString.Adapter.class)
public class ConcurrentString implements WritableString {

  private final AtomicReference<String> val;

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ConcurrentString(String val) {
    this.val = new AtomicReference<>(val);
  }

  @Override
  public String get() {
    return val.get();
  }

  @Override
  public void set(String val) {
    this.val.set(val);
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
  public boolean compareAndSet(String expect, String update) {
    while (true) {
      String cur = val.get();
      if (!Objects.equals(cur, expect)) {
        return false;
      }
      if (val.compareAndSet(cur, update)) {
        return true;
      }
    }
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
  public String getAndAccumulate(String x, BinaryOperator<String> accumulatorFunction) {
    return val.getAndAccumulate(x, accumulatorFunction);
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public String getAndSet(String newValue) {
    return val.getAndSet(newValue);
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public String getAndUpdate(UnaryOperator<String> updateFunction) {
    return val.getAndUpdate(updateFunction);
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public String updateAndGet(UnaryOperator<String> updateFunction) {
    return val.updateAndGet(updateFunction);
  }

  /** {@link ConcurrentString} を JSON の文字列として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<ConcurrentString> {

    @Override
    public void write(JsonWriter out, ConcurrentString value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.get());
    }

    @Override
    public ConcurrentString read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ConcurrentString(in.nextString());
    }
  }
}
