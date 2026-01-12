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

import java.util.function.BiPredicate;

/**
 * 例外をスローする {@link BiPredicate} を表す関数型インターフェース.
 *
 * @param <T> 第 1 引数の型
 * @param <U> 第 2 引数の型
 * @param <E> スローする例外の型
 * @author K.Koike
 */
@FunctionalInterface
public interface ThrowingBiPredicate<T, U, E extends Throwable> extends BiPredicate<T, U> {

  boolean testOrThrow(T t, U u) throws E;

  @Override
  default boolean test(T t, U u) {
    try {
      return testOrThrow(t, u);
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
    return false;
  }

  /** {@link ThrowingBiPredicate} を {@link BiPredicate} に変換する. */
  static <T, U, E extends Throwable> BiPredicate<T, U> unchecked(
      ThrowingBiPredicate<T, U, E> predicate) {
    return predicate;
  }
}
