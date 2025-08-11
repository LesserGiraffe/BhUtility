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
import java.util.function.BiConsumer;

/**
 * {@link BiConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を提供するクラス.
 *
 * @author K.Koike
 */
public class BiConsumerInvoker<U, V> {

  private final Registry registry = new Registry();

  /**
   * このオブジェクトに登録されたコールバック関数を呼び出す.
   *
   * @param u コールバック関数に与える第一引数
   * @param v コールバック関数に与える第二引数
   */
  public void invoke(U u, V v) {
    registry.first.accept(u, v);
    for (var fn : new ArrayList<>(registry.funcs)) {
      fn.accept(u, v);
    }    
    registry.last.accept(u, v);
  }

  /**
   * このオブジェクトに対しコールバック関数を登録および削除するためのオブジェクトを返す.
   *
   * @return コールバック関数の登録 / 削除用オブジェクト
   */
  public Registry getRegistry() {
    return registry;
  }

  /**
   * {@link BiConsumer} 型のコールバック関数を格納するレジストリ.
   */
  public class Registry {

    private BiConsumer<? super U, ? super V> first = (u, v) -> {};
    private BiConsumer<? super U, ? super V> last = (u, v) -> {};
    private final SequencedCollection<BiConsumer<? super U, ? super V>> funcs = new ArrayList<>();

    /**
     * {@code fn} をこのレジストリに登録する.
     *
     * @param fn レジストリに登録するメソッド
     */
    public void add(BiConsumer<? super U, ? super V> fn) {
      Objects.requireNonNull(fn);
      funcs.addLast(fn);
    }

    /**
     * {@code fn} をこのレジストリから削除する.
     *
     * <p>登録に使用したメソッドは問はない.
     *
     * @param fn 削除するメソッド
     */
    public void remove(Object fn) {
      Objects.requireNonNull(fn);
      if (fn == first) {
        first = (u, v) -> {};
      }
      if (fn == last) {
        last = (u, v) -> {};
      }
      funcs.removeAll(List.of(fn));
    }

    /**
     * {@code fn} をこのレジストリに登録する.
     * 
     * <p>このメソッドで登録したコールバック関数 ({@code fn}) は, {@link BiConsumerInvoker} により最初に呼び出されることが保証される.<br>
     * 既にこのメソッドで登録されたコールバック関数がある場合, 最初に呼び出されるコールバック関数は新しいものに置き換わる.
     *
     * @param fn レジストリに追加するメソッド
     */
    public void setFirst(BiConsumer<? super U, ? super V> fn) {
      Objects.requireNonNull(fn);
      first = fn;
    }

    /**
     * {@code fn} をこのレジストリに登録する.
     *
     * <p>このメソッドで登録したメソッド ({@code fn}) は, {@link BiConsumerInvoker} により最後に呼び出されることが保証される.<br>
     * 既にこのメソッドで登録されたコールバック関数がある場合, 最後に呼び出されるコールバック関数は新しいものに置き換わる.
     *
     * @param fn レジストリに追加するメソッド
     */
    public void setLast(BiConsumer<? super U, ? super V> fn) {
      Objects.requireNonNull(fn);
      last = fn;
    }
  }
}
