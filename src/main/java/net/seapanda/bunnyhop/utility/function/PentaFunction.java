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

import java.util.Objects;
import java.util.function.Function;

/**
 * 5 つの引数を受け取り, 結果を返す関数を表す関数型インターフェース.
 *
 * @param <S> 第 1 引数の型
 * @param <T> 第 2 引数の型
 * @param <U> 第 3 引数の型
 * @param <V> 第 4 引数の型
 * @param <W> 第 5 引数の型
 * @param <R> 結果の型
 * @author K.Koike
 */
@FunctionalInterface
public interface PentaFunction<S, T, U, V, W, R> {

  R apply(S s, T t, U u, V v, W w);

  /** {@link #apply} の戻り値を {@code after} に適用する合成関数を返す. */
  default <X> PentaFunction<S, T, U, V, W, X> andThen(Function<? super R, ? extends X> after) {
    Objects.requireNonNull(after);
    return (s, t, u, v, w) -> after.apply(this.apply(s, t, u, v, w));
  }
}
