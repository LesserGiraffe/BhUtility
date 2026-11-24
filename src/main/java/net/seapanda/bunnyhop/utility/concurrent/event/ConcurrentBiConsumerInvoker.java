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
 * <p>コールバック関数の中で {@link #invoke} を呼び出した場合, その呼び出しは現在のコールバック関数列を
 * 呼び出し終えてから処理される. <br>
 * このため, 各コールバック関数は {@link #invoke} に渡された引数を, 渡された順に受け取る.
 *
 * <p>コールバック関数が例外を投げた場合, 未処理の {@link #invoke} の要求は破棄される.
 *
 * @author K.Koike
 */
public class ConcurrentBiConsumerInvoker<U, V> extends BiConsumerInvoker<U, V> {

  private final Registry registry = new Registry();
  /** {@link #invoke} の実行中に要求された呼び出しの引数を, 要求された順に格納するキュー. */
  private final SequencedCollection<Args<U, V>> pending = new ArrayList<>();
  /** {@link #invoke} を実行中の場合 true. */
  private boolean invoking;

  @Override
  public synchronized void invoke(U u, V v) {
    pending.addLast(new Args<>(u, v));
    // 既に呼び出し中の場合, 現在のコールバック関数列を呼び出し終えてから引数を処理する.
    if (invoking) {
      return;
    }
    invoking = true;
    try {
      do {
        invokeAll(pending.removeFirst());
      } while (!pending.isEmpty());
    } finally {
      invoking = false;
      pending.clear();
    }
  }

  /**
   * このオブジェクトに登録された全てのコールバック関数を {@code args} を引数にして呼び出す.
   *
   * @param args コールバック関数に与える引数
   */
  private void invokeAll(Args<U, V> args) {
    registry.getFirst().accept(args.u(), args.v());
    registry.getFuncs().forEach(fn -> fn.accept(args.u(), args.v()));
    registry.getLast().accept(args.u(), args.v());
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /** {@link #invoke} に渡された引数の組. */
  private record Args<U, V>(U u, V v) {}

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
