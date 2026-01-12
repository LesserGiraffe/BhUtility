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

/**
 * 例外をスローする {@link PentaConsumer} を表す関数型インターフェース.
 *
 * @param <S> 第 1 引数の型
 * @param <T> 第 2 引数の型
 * @param <U> 第 3 引数の型
 * @param <V> 第 4 引数の型
 * @param <W> 第 5 引数の型
 * @param <E> スローする例外の型
 * @author K.Koike
 */
@FunctionalInterface
public interface ThrowingPentaConsumer<S, T, U, V, W, E extends Throwable>
    extends PentaConsumer<S, T, U, V, W> {

  void acceptOrThrow(S s, T t, U u, V v, W w) throws E;

  @Override
  default void accept(S s, T t, U u, V v, W w) {
    try {
      acceptOrThrow(s, t, u, v, w);
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
  }

  /** {@link ThrowingPentaConsumer} を {@link PentaConsumer} に変換する. */
  static <S, T, U, V, W, E extends Throwable> PentaConsumer<S, T, U, V, W> unchecked(
      ThrowingPentaConsumer<S, T, U, V, W, E> consumer) {
    return consumer;
  }
}
