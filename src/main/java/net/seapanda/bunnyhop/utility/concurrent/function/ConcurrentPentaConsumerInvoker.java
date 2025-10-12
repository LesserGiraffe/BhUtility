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

package net.seapanda.bunnyhop.utility.concurrent.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import net.seapanda.bunnyhop.utility.function.PentaConsumer;
import net.seapanda.bunnyhop.utility.function.PentaConsumerInvoker;

/**
 * {@link PentaConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス. <br></>
 * スレッドセーフであることを保証する.
 *
 * @author K.Koike
 */
public class ConcurrentPentaConsumerInvoker<S, T, U, V, W>
    extends PentaConsumerInvoker<S, T, U, V, W> {

  private final Registry registry = new Registry();

  @Override
  public synchronized void invoke(S s, T t, U u, V v, W w) {
    registry.getFirst().accept(s, t, u, v, w);
    registry.getFuncs().forEach(fn -> fn.accept(s, t, u, v, w));
    registry.getLast().accept(s, t, u, v, w);
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /**
   * {@link PentaConsumer} 型のコールバック関数を格納するレジストリ.
   */
  public class Registry extends PentaConsumerInvoker<S, T, U, V, W>.Registry {

    private PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> first =
        (s, t, u, v, w) -> {};
    private PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> last =
        (s, t, u, v, w) -> {};
    private final SequencedCollection<PentaConsumer<
        ? super S, ? super T, ? super U, ? super V, ? super W>> funcs = new ArrayList<>();


    @Override
    public synchronized void add(
        PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    @Override
    public synchronized void remove(Object fn) {
      Objects.requireNonNull(fn);
      if (fn == first) {
        first = (s, t, u, v, w) -> {};
      }
      if (fn == last) {
        last = (s, t, u, v, w) -> {};
      }
      funcs.removeAll(List.of(fn));
    }

    @Override
    public synchronized void setFirst(
        PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    @Override
    public synchronized void setLast(
        PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }

    private synchronized
        PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> getFirst() {
      return first;
    }

    private synchronized
        PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> getLast() {
      return last;
    }

    private synchronized
        SequencedCollection<PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W>>
        getFuncs() {
      return new ArrayList<>(funcs);
    }
  }
}
