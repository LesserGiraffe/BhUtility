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
import net.seapanda.bunnyhop.utility.function.PentaConsumer;

/**
 * {@link PentaConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス.
 *
 * <p>コールバック関数の中で {@link #invoke} を呼び出した場合, その呼び出しは現在のコールバック関数列を
 * 呼び出し終えてから処理される. <br>
 * このため, 各コールバック関数は {@link #invoke} に渡された引数を, 渡された順に受け取る.
 *
 * <p>コールバック関数が例外を投げた場合, 未処理の {@link #invoke} の要求は破棄される.
 *
 * @author K.Koike
 */
public class SimplePentaConsumerInvoker<S, T, U, V, W> extends PentaConsumerInvoker<S, T, U, V, W> {

  private final Registry registry = new Registry();
  /** {@link #invoke} の実行中に要求された呼び出しの引数を, 要求された順に格納するキュー. */
  private final SequencedCollection<Args<S, T, U, V, W>> pending = new ArrayList<>();
  /** {@link #invoke} を実行中の場合 true. */
  private boolean invoking;

  @Override
  public void invoke(S s, T t, U u, V v, W w) {
    pending.addLast(new Args<>(s, t, u, v, w));
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
  private void invokeAll(Args<S, T, U, V, W> args) {
    registry.first.accept(args.s(), args.t(), args.u(), args.v(), args.w());
    List.copyOf(registry.funcs)
        .forEach(fn -> fn.accept(args.s(), args.t(), args.u(), args.v(), args.w()));
    registry.last.accept(args.s(), args.t(), args.u(), args.v(), args.w());
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  /** {@link #invoke} に渡された引数の組. */
  private record Args<S, T, U, V, W>(S s, T t, U u, V v, W w) {}

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
    public void add(PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    @Override
    public void remove(Object fn) {
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
    public void setFirst(PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    @Override
    public void setLast(PentaConsumer<? super S, ? super T, ? super U, ? super V, ? super W> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }
  }
}
