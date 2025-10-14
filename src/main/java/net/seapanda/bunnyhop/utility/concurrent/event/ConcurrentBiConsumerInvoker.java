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

package net.seapanda.bunnyhop.utility.concurrent.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.BiConsumer;
import net.seapanda.bunnyhop.utility.event.BiConsumerInvoker;

/**
 * {@link BiConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス.
 * スレッドセーフであることを保証する.
 *
 * @author K.Koike
 */
public class ConcurrentBiConsumerInvoker<U, V> extends BiConsumerInvoker<U, V> {

  private final Registry registry = new Registry();

  @Override
  public synchronized void invoke(U u, V v) {
    registry.getFirst().accept(u, v);
    registry.getFuncs().forEach(fn -> fn.accept(u, v));
    registry.getLast().accept(u, v);
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /**
   * {@link BiConsumer} 型のコールバック関数を格納するレジストリ.
   */
  public class Registry extends BiConsumerInvoker<U, V>.Registry {

    private BiConsumer<? super U, ? super V> first = (u, v) -> {};
    private BiConsumer<? super U, ? super V> last = (u, v) -> {};
    private final SequencedCollection<BiConsumer<? super U, ? super V>> funcs = new ArrayList<>();

    @Override
    public synchronized void add(BiConsumer<? super U, ? super V> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    @Override
    public synchronized void remove(Object fn) {
      Objects.requireNonNull(fn);
      if (fn == first) {
        first = (u, v) -> {};
      }
      if (fn == last) {
        last = (u, v) -> {};
      }
      funcs.removeAll(List.of(fn));
    }

    @Override
    public synchronized void setFirst(BiConsumer<? super U, ? super V> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    @Override
    public synchronized void setLast(BiConsumer<? super U, ? super V> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }

    private synchronized BiConsumer<? super U, ? super V> getFirst() {
      return first;
    }

    private synchronized BiConsumer<? super U, ? super V> getLast() {
      return last;
    }

    private synchronized SequencedCollection<BiConsumer<? super U, ? super V>> getFuncs() {
      return new ArrayList<>(funcs);
    }
  }

}
