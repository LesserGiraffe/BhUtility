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
import java.util.function.UnaryOperator;
import net.seapanda.bunnyhop.utility.reference.WritableBoolean;

/**
 * スレッドセーフに読み書きできる {@code boolean} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の真偽値としてシリアライズ/デシリアライズされる.
 *
 * @author K.Koike
 */
@JsonAdapter(ConcurrentBoolean.Adapter.class)
public class ConcurrentBoolean implements WritableBoolean {

  private final AtomicInteger val;

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ConcurrentBoolean(boolean val) {
    this.val = new AtomicInteger(toInt(val));
  }

  @Override
  public boolean get() {
    return toBoolean(val.get());
  }

  @Override
  public void set(boolean val) {
    this.val.set(toInt(val));
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public boolean compareAndSet(boolean expect, boolean update) {
    return val.compareAndSet(toInt(expect), toInt(update));
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public boolean getAndSet(boolean newValue) {
    return toBoolean(this.val.getAndSet(toInt(newValue)));
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数. {@code null} を返してはいけない.
   * @return 更新前の値
   */
  public boolean getAndUpdate(UnaryOperator<Boolean> updateFunction) {
    return toBoolean(val.getAndUpdate(cur -> toInt(updateFunction.apply(toBoolean(cur)))));
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数. {@code null} を返してはいけない.
   * @return 更新後の値
   */
  public boolean updateAndGet(UnaryOperator<Boolean> updateFunction) {
    return toBoolean(val.updateAndGet(cur -> toInt(updateFunction.apply(toBoolean(cur)))));
  }

  /**
   * このオブジェクトが保持する値を反転する.
   *
   * @return 反転後の値
   */
  public boolean negate() {
    return toBoolean(val.updateAndGet(cur -> cur == 0 ? 1 : 0));
  }

  private static int toInt(boolean val) {
    return val ? 1 : 0;
  }

  private static boolean toBoolean(int val) {
    return val != 0;
  }

  /** {@link ConcurrentBoolean} を JSON の真偽値として入出力する {@link TypeAdapter}. */
  static class Adapter extends TypeAdapter<ConcurrentBoolean> {

    @Override
    public void write(JsonWriter out, ConcurrentBoolean value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value.get());
    }

    @Override
    public ConcurrentBoolean read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ConcurrentBoolean(in.nextBoolean());
    }
  }
}
