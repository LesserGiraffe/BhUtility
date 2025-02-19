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

package net.seapanda.bunnyhop.utility;

import java.util.Objects;

/**
 * 4 つの引数をとるコンシューマメソッド.
 *
 * @author K.Koike
 */
@FunctionalInterface
public interface TetraConsumer<T, U, V, W> {
  
  void accept(T t, U u, V v, W w);

  /**
   * このオブジェクトの {@link #accept} の後に {@link #accept} と同じ引数で
   * {@code after} を呼ぶメソッドを返す.
   */
  default TetraConsumer<T, U, V, W> andThen(
      final TetraConsumer<? super T, ? super U, ? super V, ? super W> after) {
    Objects.requireNonNull(after);
    return (t, u, v, w) -> {
      accept(t, u, v, w);
      after.accept(t, u, v, w);
    };
  }
}
