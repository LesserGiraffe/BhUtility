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

package net.seapanda.bunnyhop.utility.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import net.seapanda.bunnyhop.utility.function.TetraConsumer;

/**
 * {@link TetraConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス.
 *
 * @author K.Koike
 */
public class SimpleTetraConsumerInvoker<T, U, V, W> extends TetraConsumerInvoker<T, U, V, W> {

  private final Registry registry = new Registry();

  @Override
  public void invoke(T t, U u, V v, W w) {
    registry.first.accept(t, u, v, w);
    List.copyOf(registry.funcs).forEach(fn -> fn.accept(t, u, v, w));
    registry.last.accept(t, u, v, w);
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /**
   * {@link TetraConsumer} 型のコールバック関数を格納するレジストリ.
   */
  public class Registry extends TetraConsumerInvoker<T, U, V, W>.Registry {

    private TetraConsumer<? super T, ? super U, ? super V, ? super W> first = (t, u, v, w) -> {};
    private TetraConsumer<? super T, ? super U, ? super V, ? super W> last = (t, u, v, w) -> {};
    private final SequencedCollection<TetraConsumer<? super T, ? super U, ? super V, ? super W>>
        funcs = new ArrayList<>();

    @Override
    public void add(TetraConsumer<? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    @Override
    public void remove(Object fn) {
      Objects.requireNonNull(fn);
      if (fn == first) {
        first = (t, u, v, w) -> {};
      }
      if (fn == last) {
        last = (t, u, v, w) -> {};
      }
      funcs.removeAll(List.of(fn));
    }

    @Override
    public void setFirst(TetraConsumer<? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    @Override
    public void setLast(TetraConsumer<? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }
  }
}
