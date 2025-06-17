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

package net.seapanda.bunnyhop.utility;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 連番を生成する機能を提供するクラス.
 *
 * @author K.Koike
 */
public class SerialNumber implements Serializable {
  
  private static AtomicLong serialNumber = new AtomicLong(0);
  private final String hexStr;
  private final long val;

  /** {@link SerialNumber} を生成する. */
  public static SerialNumber newNumber() {
    return new SerialNumber(serialNumber.addAndGet(1));
  }

  private SerialNumber(long num) {
    this.val = num;
    this.hexStr = Long.toHexString(num);
  }

  /** デフォルトコンストラクタ. (デシリアライズ用) */
  public SerialNumber() {
    val = 0;
    hexStr = "";
  }

  /** このオブジェクトが持つ番号を取得する. */
  public long value() {
    return val;
  }

  /** このオブジェクトが持つ番号の 16 進表現を取得する. */
  public String hexStr() {
    return hexStr;
  }

  @Override
  public String toString() {
    return Long.toString(val);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return (getClass() == obj.getClass()) && (val == ((SerialNumber) obj).val);
  }

  @Override
  public int hashCode() {
    return Long.hashCode(val);
  }
}
