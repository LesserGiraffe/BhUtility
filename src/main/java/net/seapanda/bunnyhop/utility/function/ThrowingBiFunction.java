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

import java.util.function.BiFunction;

/**
 * 例外をスローする {@link BiFunction} を表す関数型インターフェース.
 *
 * @param <T> 第 1 引数の型
 * @param <U> 第 2 引数の型
 * @param <R> 結果の型
 * @param <E> スローする例外の型
 * @author K.Koike
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Throwable> extends BiFunction<T, U, R> {

  R applyOrThrow(T t, U u) throws E;

  @Override
  default R apply(T t, U u) {
    try {
      return applyOrThrow(t, u);
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
    return null;
  }

  /** {@link ThrowingBiFunction} を {@link BiFunction} に変換する. */
  static <T, U, R, E extends Throwable> BiFunction<T, U, R> unchecked(
      ThrowingBiFunction<T, U, R, E> function) {
    return function;
  }
}
