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

package net.seapanda.bunnyhop.utility.function;

import java.util.function.BinaryOperator;

/**
 * 例外をスローする {@link BinaryOperator} を表す関数型インターフェース.
 *
 * @param <T> オペランドと結果の型
 * @param <E> スローする例外の型
 */
public interface ThrowingBinaryOperator<T, E extends Throwable> extends BinaryOperator<T> {

  T applyOrThrow(T t0, T t1) throws E;

  @Override
  default T apply(T t0, T t1) {
    try {
      return applyOrThrow(t0, t1);
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
    return null;
  }

  /** {@link ThrowingBinaryOperator} を {@link BinaryOperator} に変換する. */
  static <T, E extends Exception> BinaryOperator<T> unchecked(
      ThrowingBinaryOperator<T, E> operator) {
    return operator;
  }
}
