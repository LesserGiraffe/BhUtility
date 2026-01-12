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

import java.util.function.Consumer;

/**
 * 例外をスローする {@link Consumer} を表す関数型インターフェース.
 *
 * @param <T> 入力の型
 * @param <E> スローする例外の型
 * @author K.Koike
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> extends Consumer<T> {

  void acceptOrThrow(T t) throws E;

  @Override
  default void accept(T t) {
    try {
      acceptOrThrow(t);
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
  }

  /** {@link ThrowingConsumer} を {@link Consumer} に変換する. */
  static <T, E extends Throwable> Consumer<T> unchecked(ThrowingConsumer<T, E> consumer) {
    return consumer;
  }
}
