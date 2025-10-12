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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;

/**
 * {@link TriConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス.
 *
 * @author K.Koike
 */
public class SimpleTriConsumerInvoker<U, V, W> extends TriConsumerInvoker<U, V, W> {

  private final Registry registry = new Registry();

  @Override
  public void invoke(U u, V v, W w) {
    registry.first.accept(u, v, w);
    List.copyOf(registry.funcs).forEach(fn -> fn.accept(u, v, w));
    registry.last.accept(u, v, w);
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /**
   * {@link TriConsumer} 型のコールバック関数を格納するレジストリ.
   */
  public class Registry extends TriConsumerInvoker<U, V, W>.Registry {

    private TriConsumer<? super U, ? super V, ? super W> first = (u, v, w) -> {};
    private TriConsumer<? super U, ? super V, ? super W> last = (u, v, w) -> {};
    private final SequencedCollection<TriConsumer<? super U, ? super V, ? super W>> funcs =
        new ArrayList<>();

    @Override
    public void add(TriConsumer<? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    @Override
    public void remove(Object fn) {
      Objects.requireNonNull(fn);
      if (fn == first) {
        first = (u, v, w) -> {};
      }
      if (fn == last) {
        last = (u, v, w) -> {};
      }
      funcs.removeAll(List.of(fn));
    }

    @Override
    public void setFirst(TriConsumer<? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    @Override
    public void setLast(TriConsumer<? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }
  }
}
