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
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.event.SimpleConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.WritableDouble;

/**
 * スレッドセーフに読み書きでき, 値の変更を監視できる {@code double} 値.
 *
 * <p>Gson では, オブジェクトではなく JSON の数値としてシリアライズ/デシリアライズされる.
 * {@code NaN} や {@code Infinity} の扱いは {@code double} 型のフィールドと同じになるため,
 * これらを出力するには {@code GsonBuilder.serializeSpecialFloatingPointValues} を設定する必要がある.
 *
 * <p>値の等値比較は {@code ==} 演算子で行うため, 以下の点に注意すること.<br>
 * ・{@code NaN} から {@code NaN} への変更ではイベントハンドラが呼ばれる.<br>
 * ・{@code 0.0} から {@code -0.0} への変更ではイベントハンドラが呼ばれない.<br>
 * ・{@link #compareAndSet} の {@code expect} に {@code NaN} を指定した場合, 値は更新されない.
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
@JsonAdapter(ObservableAtomicDouble.Adapter.class)
public class ObservableAtomicDouble implements WritableDouble {

  private double val;
  private final ConsumerInvoker<ValueChangeEvent> invoker = new SimpleConsumerInvoker<>();

  /**
   * コンストラクタ.
   *
   * @param val このオブジェクトが保持する値の初期値
   */
  public ObservableAtomicDouble(double val) {
    this.val = val;
  }

  @Override
  public synchronized double get() {
    return val;
  }

  @Override
  public synchronized void set(double val) {
    double oldVal = this.val;
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
   * <p>等値比較は {@code ==} 演算子で行うため, 以下の点に注意すること.<br>
   * ・{@code NaN} は {@code NaN} と等しくないとみなす. このため, {@code expect} に {@code NaN} を
   * 指定した場合, 値は更新されない.<br>
   * ・{@code 0.0} と {@code -0.0} は等しいとみなす.
   *
   * @param expect このオブジェクトが保持する値と比較する値
   * @param update このオブジェクトに設定する値
   * @return 値を更新した場合 true
   */
  public synchronized boolean compareAndSet(double expect, double update) {
    if (val == expect) {
      double oldVal = val;
      val = update;
      notifyIfChanged(oldVal, update);
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
  public synchronized double getAndSet(double newValue) {
    double oldVal = val;
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
  public synchronized double getAndUpdate(DoubleUnaryOperator updateFunction) {
    double oldVal = val;
    val = updateFunction.applyAsDouble(val);
    notifyIfChanged(oldVal, val);
    return oldVal;
  }

  /**
   * このオブジェクトが保持する値に {@code updateFunction} を適用した結果を, このオブジェクトが保持する値にする.
   *
   * @param updateFunction 新しい値を計算する関数
   * @return 更新後の値
   */
  public synchronized double updateAndGet(DoubleUnaryOperator updateFunction) {
    double oldVal = val;
    val = updateFunction.applyAsDouble(val);
    notifyIfChanged(oldVal, val);
    return val;
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
   * <p>等値比較は {@code ==} 演算子で行う.
   *
   * @param oldVal 更新前の値
   * @param newVal 更新後の値
   */
  private void notifyIfChanged(double oldVal, double newVal) {
    if (oldVal != newVal) {
      invoker.invoke(new ValueChangeEvent(this, oldVal, newVal));
    }
  }

  /**
   * {@link ObservableAtomicDouble} の値が変わったときの情報を格納したレコード.
   *
   * @param observer 値が変わった {@link ObservableAtomicDouble} オブジェクト.
   * @param oldVal 変更前の値
   * @param newVal 変更後の値
   */
  public record ValueChangeEvent(
      ObservableAtomicDouble observer, double oldVal, double newVal) {}

  /**
   * {@link ObservableAtomicDouble} を JSON の数値として入出力する {@link TypeAdapter} を作成するファクトリ.
   */
  static class Adapter implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!ObservableAtomicDouble.class.equals(type.getRawType())) {
        return null;
      }
      @SuppressWarnings("unchecked")
      TypeAdapter<T> adapter =
          (TypeAdapter<T>) new ConcurrentObservableDoubleAdapter(gson.getAdapter(Double.class));
      return adapter;
    }
  }

  /**
   * {@link ObservableAtomicDouble} を JSON の数値として入出力する {@link TypeAdapter}.
   *
   * <p>値の入出力は {@code double} 用の {@link TypeAdapter} に委譲するため,
   * {@code NaN} や {@code Infinity} の扱いは {@code double} 型のフィールドと同じになる.
   */
  private static class ConcurrentObservableDoubleAdapter
      extends TypeAdapter<ObservableAtomicDouble> {

    /** {@code double} 値の入出力に使う {@link TypeAdapter}. */
    private final TypeAdapter<Double> delegate;

    private ConcurrentObservableDoubleAdapter(TypeAdapter<Double> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void write(JsonWriter out, ObservableAtomicDouble value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      delegate.write(out, value.get());
    }

    @Override
    public ObservableAtomicDouble read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new ObservableAtomicDouble(delegate.read(in));
    }
  }
}
