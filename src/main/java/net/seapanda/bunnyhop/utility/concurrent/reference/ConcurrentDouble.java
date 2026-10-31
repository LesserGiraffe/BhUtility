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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import net.seapanda.bunnyhop.utility.reference.WritableDouble;

/**
 * スレッドセーフに読み書きできる {@code double} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の数値としてシリアライズ/デシリアライズされる.
 * {@code NaN} や {@code Infinity} の扱いは {@code double} 型のフィールドと同じになるため,
 * これらを出力するには {@code GsonBuilder.serializeSpecialFloatingPointValues} を設定する必要がある.
 *
 * @author K.Koike
 */
@JsonAdapter(ConcurrentDouble.Adapter.class)
public class ConcurrentDouble implements WritableDouble {

  /** このオブジェクトが保持する値を {@link Double#doubleToLongBits} で変換したビットパターン. */
  private final AtomicLong bits;

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ConcurrentDouble(double val) {
    bits = new AtomicLong(Double.doubleToLongBits(val));
  }

  @Override
  public double get() {
    return Double.longBitsToDouble(bits.get());
  }

  @Override
  public void set(double val) {
    bits.set(Double.doubleToLongBits(val));
  }

  /**
   * このオブジェクトが保持する値と {@code x} に {@code accumulatorFunction} を適用した結果を,
   * このオブジェクトが保持する値にする.
   *
   * @param x {@code accumulatorFunction} の第 2 引数に渡す値
   * @param accumulatorFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public double accumulateAndGet(double x, DoubleBinaryOperator accumulatorFunction) {
    return updateAndGet(cur -> accumulatorFunction.applyAsDouble(cur, x));
  }

  /**
   * このオブジェクトが保持する値と {@code x} に {@code accumulatorFunction} を適用した結果を,
   * このオブジェクトが保持する値にする.
   *
   * @param x {@code accumulatorFunction} の第 2 引数に渡す値
   * @param accumulatorFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public double getAndAccumulate(double x, DoubleBinaryOperator accumulatorFunction) {
    return getAndUpdate(cur -> accumulatorFunction.applyAsDouble(cur, x));
  }

  /**
   * このオブジェクトが保持する値が {@code expect} と等しい場合に限り, 保持する値を {@code update} にする.
   *
   * <p>等値比較は {@link Double#doubleToLongBits} で求めたビットパターンに対して行うため,
   * {@code ==} 演算子による比較とは以下の点で異なる.<br>
   * ・{@code NaN} は {@code NaN} と等しいとみなす.<br>
   * ・{@code 0.0} と {@code -0.0} は等しくないとみなす.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public boolean compareAndSet(double expect, double update) {
    return bits.compareAndSet(Double.doubleToLongBits(expect), Double.doubleToLongBits(update));
  }

  /**
   * このオブジェクトが保持する値を {@code newValue} にする.
   *
   * @param newValue このオブジェクトに設定する値
   * @return 更新前の値
   */
  public double getAndSet(double newValue) {
    return Double.longBitsToDouble(bits.getAndSet(Double.doubleToLongBits(newValue)));
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新前の値
   */
  public double getAndUpdate(DoubleUnaryOperator updateFunction) {
    long oldVal = bits.getAndUpdate(
        curLong -> {
          double curDouble = Double.longBitsToDouble(curLong);
          return Double.doubleToLongBits(updateFunction.applyAsDouble(curDouble));
        });
    return Double.longBitsToDouble(oldVal);

  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public double updateAndGet(DoubleUnaryOperator updateFunction) {
    long newVal = bits.updateAndGet(
        curLong -> {
          double curDouble = Double.longBitsToDouble(curLong);
          return Double.doubleToLongBits(updateFunction.applyAsDouble(curDouble));
        });
    return Double.longBitsToDouble(newVal);
  }

  /**
   * このオブジェクトが保持する値に {@code delta} を加算する.
   *
   * @param delta このオブジェクトが保持する値に加算する値
   * @return 加算前の値
   */
  public double getAndAdd(double delta) {
    return getAndAccumulate(delta, Double::sum);
  }

  /**
   * このオブジェクトが保持する値に {@code delta} を加算する.
   *
   * @param delta このオブジェクトが保持する値に加算する値
   * @return 加算後の値
   */
  public double addAndGet(double delta) {
    return accumulateAndGet(delta, Double::sum);
  }

  /** {@link ConcurrentDouble} を JSON の数値として入出力する {@link TypeAdapter} を作成するファクトリ. */
  static class Adapter implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!ConcurrentDouble.class.equals(type.getRawType())) {
        return null;
      }
      @SuppressWarnings("unchecked")
      TypeAdapter<T> adapter =
          (TypeAdapter<T>) new ConcurrentDoubleAdapter(gson.getAdapter(Double.class));
      return adapter;
    }
  }

  /**
   * {@link ConcurrentDouble} を JSON の数値として入出力する {@link TypeAdapter}.
   *
   * <p>値の入出力は {@code double} 用の {@link TypeAdapter} に委譲するため,
   * {@code NaN} や {@code Infinity} の扱いは {@code double} 型のフィールドと同じになる.
   */
  private static class ConcurrentDoubleAdapter extends TypeAdapter<ConcurrentDouble> {

    /** {@code double} 値の入出力に使う {@link TypeAdapter}. */
    private final TypeAdapter<Double> delegate;

    private ConcurrentDoubleAdapter(TypeAdapter<Double> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void write(JsonWriter out, ConcurrentDouble value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      delegate.write(out, value.get());
    }

    @Override
    public ConcurrentDouble read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ConcurrentDouble(delegate.read(in));
    }
  }
}
