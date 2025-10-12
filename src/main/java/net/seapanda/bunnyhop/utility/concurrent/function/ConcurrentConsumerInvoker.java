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
import java.util.function.Consumer;
import net.seapanda.bunnyhop.utility.function.ConsumerInvoker;

/**
 * {@link Consumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス. <br>
 * スレッドセーフであることを保証する.
 *
 * @author K.Koike
 */
public class ConcurrentConsumerInvoker<U> extends ConsumerInvoker<U> {

  private final Registry registry = new Registry();

  @Override
  public synchronized void invoke(U u) {
    // コールバック内で登録した後続のコールバックを呼び出せるようにする.
    registry.getFirst().accept(u);
    registry.getFuncs().forEach(fn -> fn.accept(u));
    registry.getLast().accept(u);
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /**
   * {@link Consumer} 型のコールバック関数を格納するレジストリ.
   */
  public class Registry extends ConsumerInvoker<U>.Registry {

    private Consumer<? super U> first = u -> {};
    private Consumer<? super U> last = u -> {};
    private final SequencedCollection<Consumer<? super U>> funcs = new ArrayList<>();

    @Override
    public synchronized void add(Consumer<? super U> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    @Override
    public synchronized void remove(Object fn) {
      Objects.requireNonNull(fn);
      if (fn == first) {
        first = u -> {};
      }
      if (fn == last) {
        last = u -> {};
      }
      funcs.removeAll(List.of(fn));
    }

    @Override
    public synchronized void setFirst(Consumer<? super U> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    @Override
    public synchronized void setLast(Consumer<? super U> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }

    private synchronized Consumer<? super U> getFirst() {
      return first;
    }

    private synchronized Consumer<? super U> getLast() {
      return last;
    }

    private synchronized SequencedCollection<Consumer<? super U>> getFuncs() {
      return new ArrayList<>(funcs);
    }
  }
}
