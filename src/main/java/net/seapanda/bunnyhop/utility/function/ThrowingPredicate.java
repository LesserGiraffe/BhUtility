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

import java.util.function.Predicate;

/**
 * 例外をスローする {@link Predicate} を表す関数型インターフェース.
 *
 * @param <T> 入力の型
 * @param <E> スローする例外の型
 * @author K.Koike
 */
@FunctionalInterface
public interface ThrowingPredicate<T, E extends Throwable> extends Predicate<T> {

  boolean testOrThrow(T t) throws E;

  @Override
  default boolean test(T t) {
    try {
      return testOrThrow(t);
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
    return false;
  }

  /** {@link ThrowingPredicate} を {@link Predicate} に変換する. */
  static <T, E extends Throwable> Predicate<T> unchecked(ThrowingPredicate<T, E> predicate) {
    return predicate;
  }
}
