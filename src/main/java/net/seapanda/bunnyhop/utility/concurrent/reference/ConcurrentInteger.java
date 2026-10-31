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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import net.seapanda.bunnyhop.utility.reference.WritableInteger;

/**
 * スレッドセーフに読み書きできる {@code int} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の数値としてシリアライズ/デシリアライズされる.
 *
 * @author K.Koike
 */
@JsonAdapter(ConcurrentInteger.Adapter.class)
public class ConcurrentInteger implements WritableInteger {

  private final AtomicInteger val;

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ConcurrentInteger(int val) {
    this.val = new AtomicInteger(val);
  }

  @Override
  public int get() {
    return val.get();
  }


  @Override
  public void set(int val) {
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
  public int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
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
  public int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
    return val.getAndAccumulate(x, accumulatorFunction);
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public boolean compareAndSet(int expect, int update) {
    return val.compareAndSet(expect, update);
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public int getAndSet(int newValue) {
    return val.getAndSet(newValue);
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public int getAndUpdate(IntUnaryOperator updateFunction) {
    return val.getAndUpdate(updateFunction);
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public int updateAndGet(IntUnaryOperator updateFunction) {
    return val.updateAndGet(updateFunction);
  }

  /**
   * このオブジェクトが保持する値に {@code delta} を加算する.
   *
   * @param delta このオブジェクトが保持する値に加算する値
   * @return 加算前の値
   */
  public int getAndAdd(int delta) {
    return val.getAndAdd(delta);
  }

  /**
   * このオブジェクトが保持する値に {@code delta} を加算する.
   *
   * @param delta このオブジェクトが保持する値に加算する値
   * @return 加算後の値
   */
  public int addAndGet(int delta) {
    return val.addAndGet(delta);
  }

  /** {@link ConcurrentInteger} を JSON の数値として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<ConcurrentInteger> {

    @Override
    public void write(JsonWriter out, ConcurrentInteger value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.get());
    }

    @Override
    public ConcurrentInteger read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ConcurrentInteger(in.nextInt());
    }
  }
}
