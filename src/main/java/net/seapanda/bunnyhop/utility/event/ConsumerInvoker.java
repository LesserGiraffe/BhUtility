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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link BiConsumer} 型のコールバック関数の登録, 削除および呼び出し機能を規定したクラス.
 *
 * @author K.Koike
 */
public abstract class ConsumerInvoker<U> {

  /**
   * このオブジェクトに登録されたコールバック関数を呼び出す.
   *
   * @param u コールバック関数に与える引数
   */
  public abstract void invoke(U u);

  /**
   * このオブジェクトに対しコールバック関数を登録および削除するためのオブジェクトを返す.
   *
   * @return コールバック関数の登録 / 削除用オブジェクト
   */
  public abstract Registry getRegistry();

  /**
   * {@link Consumer} 型のコールバック関数を格納するレジストリ.
   */
  public abstract class Registry {

    /**
     * {@code fn} をこのレジストリに登録する.
     *
     * @param fn レジストリに登録するメソッド
     */
    public abstract void add(Consumer<? super U> fn);

    /**
     * {@code fn} をこのレジストリから削除する.
     *
     * <p>登録に使用したメソッドは問わない.
     *
     * @param fn 削除するメソッド
     */
    public abstract void remove(Object fn);

    /**
     * {@code fn} をこのレジストリに登録する.
     *
     * <p>このメソッドで登録したコールバック関数 ({@code fn}) は, {@link ConsumerInvoker} により最初に呼び出されることが保証される.<br>
     * 既にこのメソッドで登録されたコールバック関数がある場合, 最初に呼び出されるコールバック関数は新しいものに置き換わる.
     *
     * @param fn レジストリに追加するメソッド
     */
    public abstract void setFirst(Consumer<? super U> fn);

    /**
     * {@code fn} をこのレジストリに登録する.
     *
     * <p>このメソッドで登録したメソッド ({@code fn}) は, {@link ConsumerInvoker} により最後に呼び出されることが保証される.<br>
     * 既にこのメソッドで登録されたコールバック関数がある場合, 最後に呼び出されるコールバック関数は新しいものに置き換わる.
     *
     * @param fn レジストリに追加するメソッド
     */
    public abstract void setLast(Consumer<? super U> fn);
  }
}
