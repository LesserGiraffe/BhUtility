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

package net.seapanda.bunnyhop.utility.reference;

import java.util.function.Consumer;

/**
 * 値の変更を監視できる {@link String} 値.
 *
 * @author K.Koike
 */
public interface ObservableString extends WritableString {

  /**
   * このオブジェクトの値が変わったときのイベントハンドラを追加する.
   *
   * @param handler 追加するイベントハンドラ
   */
  void addOnValueChanged(Consumer<? super ValueChangeEvent> handler);

  /**
   * このオブジェクトの値が変わったときのイベントハンドラを削除する.
   *
   * @param handler 削除するイベントハンドラ
   */
  void removeOnValueChanged(Object handler);

  /**
   * {@link ObservableString} の値が変わったときの情報を格納するクラス.
   *
   * @param observer 値が変わった {@link ObservableString} オブジェクト
   * @param oldVal   変更前の値
   * @param newVal   変更後の値
   */
  record ValueChangeEvent(ObservableString observer, String oldVal, String newVal) {}
}
