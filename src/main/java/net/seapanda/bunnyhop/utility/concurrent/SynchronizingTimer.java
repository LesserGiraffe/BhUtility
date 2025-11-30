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

package net.seapanda.bunnyhop.utility.concurrent;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 同期タイマー.
 *
 * <pre>
 * タイマー値が 0 になるまでスレッドをブロックする機能を持つ.
 * タイマー値は, カウントダウンとリセットにより変更可能.
 * タイマー値が 0 になったとき, 自動的にリセットするかどうかを設定可能.
 * 自動的にリセットするときタイマーの値は, 最後にリセットした値となる.
 * </pre>
 *
 * @author K.Koike
 */
public final class SynchronizingTimer {

  /** タイマーのカウンタの最小値. */
  public static final int MIN_COUNT = 0;
  /** タイマーのカウンタの最大値.  この値は {@link Phaser} の実装による制限から来る. */
  public static final int MAX_COUNT = 65535;

  private final Phaser phaser;
  private int resetVal;
  /** 自動リセットが有効かどうか. */
  private final boolean autoReset;

  /**
   * コンストラクタ.
   *
   * @param count タイマーの初期値. ({@link #MIN_COUNT} 以上, {@link #MAX_COUNT} 以下を指定すること)
   */
  public SynchronizingTimer(int count, boolean autoReset) {
    if (count < MIN_COUNT || count > MAX_COUNT) {
      throw new IllegalArgumentException(
          "The 'count' must be %s - %s.  (%s)".formatted(MIN_COUNT, MAX_COUNT, count));
    }
    resetVal = count;
    this.autoReset = autoReset;
    phaser = new Phaser(count) {
      @Override
      protected boolean onAdvance(int phase, int registeredParties) {
        return false;
      }
    };
  }

  /**
   * タイマーのカウントを 1 減らす.
   * 
   * <pre>
   * 既にカウントが 0 の場合は何もしない.
   * カウントを減らした結果 0 になった場合, 最後にリセットした値をカウンタ値にセットする.
   * </pre>
   */
  public void countdown() {
    deregister();
  }

  /**
   * {@code  phaser} の登録済みパーティ数を 1 減らして, 減らす前のフェーズと登録済みパーティ数を返す.
   * <pre>
   * 既に登録済みパーティ数が 0 の場合は減らさない.
   * 自動リセットが有効でかつ登録済みパーティ数を減らしたことでフェーズが変わった場合,
   * {@code resetVal} と同じ数のパーティを {@code phaser} に登録する.
   *
   * 登録済みパーティ数とフェーズを変更する可能性のある操作と排他にすることで,
   * 呼び出した時点でのフェーズに対応する登録済みパーティ数を取得する.
   * </pre>
   *
   * @return パーティ数を減らす前の {@link Phaser} のフェーズと登録済みパーティ数
   */
  private synchronized PhaserStatus deregister() {
    int phase = phaser.getPhase();
    int registeredParties = phaser.getRegisteredParties();
    if (registeredParties == 0) {
      return new PhaserStatus(phase, registeredParties);
    }
    if (resetVal == 0) {
      throw new AssertionError();
    }
    phaser.arriveAndDeregister();
    // 登録済みパーティ数が 0 の phaser に対して awaitAdvance*() を呼び出しても待ち動作は発生するので,
    // ここで, awaitAdvance*() が呼ばれても問題ない.
    if (phaser.getRegisteredParties() == 0 && autoReset) {
      phaser.bulkRegister(resetVal);
    }
    return new PhaserStatus(phase, registeredParties);
  }

  /**
   * タイマーのカウントが 0 になるまで待つ.
   * スレッドが中断された場合は即座に制御を返す.
   */
  public void await() {
    try {
      awaitInterruptibly();
    } catch (InterruptedException e) { /* Do nothing. */ }
  }

  /**
   * タイマーのカウントが 0 になるまで {@code timeout} で指定した時間待つ.
   *
   * <p>スレッドが中断された場合は即座に制御を返す.
   * この場合戻り値は false となる.
   *
   * @param timeout 最大待ち時間
   * @param unit 待ち時間の単位
   * @return タイマーのカウントが 0 に達した場合 true を返す
   */
  public boolean await(long timeout, TimeUnit unit) {
    try {
      return awaitInterruptibly(timeout, unit);
    } catch (InterruptedException e) {
      return false;
    }
  }

  /** タイマーのカウントが 0 になるまで待つ. */
  public void awaitInterruptibly() throws InterruptedException {
    PhaserStatus status = getPhaserStatus();
    if (status.numParties() == 0) {
      return;
    }
    // ここで reset(0) によって登録済みパーティ数が 0 になっても, フェーズが変わるので,
    // 登録済みパーティ数 0 のフェーズで待ち続けることはない.
    phaser.awaitAdvanceInterruptibly(status.phase);
  }

  /**
   * タイマーのカウントが 0 になるまで {@code timeout} で指定した時間待つ.
   *
   * @param timeout 最大待ち時間
   * @param unit 待ち時間の単位
   * @return タイマーのカウントが 0 に達した場合 true を返す
   */
  public boolean awaitInterruptibly(long timeout, TimeUnit unit) throws InterruptedException {
    PhaserStatus status = getPhaserStatus();
    if (status.numParties() == 0) {
      return true;
    }
    try {
      phaser.awaitAdvanceInterruptibly(status.phase, timeout, unit);
    } catch (TimeoutException e) {
      return false;
    }
    return true;
  }

  /**
   * タイマーのカウントを 1 減らしてから, カウントが 0 になるまで待つ.
   * スレッドが中断された場合は即座に制御を返す.
   */
  public void countdownAndAwait() {
    try {
      countdownAndAwaitInterruptibly();
    } catch (InterruptedException e) { /* Do nothing. */ }
  }

  /**
   * タイマーのカウントを 1 減らしてから, カウントが 0 になるまで待つ.
   * スレッドが中断された場合は即座に制御を返す.
   *
   * @param timeout 最大待ち時間
   * @param unit 待ち時間の単位
   * @return タイマーのカウントが 0 に達した場合 true を返す
   */
  public boolean countdownAndAwait(long timeout, TimeUnit unit) {
    try {
      return countdownAndAwaitInterruptibly(timeout, unit);
    } catch (InterruptedException e) {
      return false;
    }
  }

  /** タイマーのカウントを 1 減らしてから, カウントが 0 になるまで待つ. */
  public void countdownAndAwaitInterruptibly() throws InterruptedException {
    PhaserStatus status = deregister();
    if (status.numParties() == 0) {
      return;
    }
    phaser.awaitAdvanceInterruptibly(status.phase());
  }

  /**
   * タイマーのカウントを 1 減らしてから, カウントが 0 になるまで待つ.
   *
   * @param timeout 最大待ち時間
   * @param unit 待ち時間の単位
   * @return タイマーのカウントが 0 に達した場合 true を返す
   */
  public boolean countdownAndAwaitInterruptibly(long timeout, TimeUnit unit)
      throws InterruptedException {
    PhaserStatus status = deregister();
    if (status.numParties() == 0) {
      return false;
    }
    try {
      phaser.awaitAdvanceInterruptibly(status.phase(), timeout, unit);
    } catch (TimeoutException e) {
      return false;
    }
    return true;
  }

  /**
   * タイマーをリセットする.
   *
   * @param count セットするカウンタ値. ({@link #MIN_COUNT} 以上, {@link #MAX_COUNT} 以下を指定すること)
   * @throws IllegalArgumentException {@code count} の範囲が不正な場合.
   */
  public synchronized void reset(int count) {
    if (count < MIN_COUNT || count > MAX_COUNT) {
      throw new IllegalArgumentException(
          "The 'count' must be %s - %s.  (%s)".formatted(MIN_COUNT, MAX_COUNT, count));
    }
    resetVal = count;
    int currentCount = phaser.getRegisteredParties();
    if (count > currentCount) {
      phaser.bulkRegister(count - currentCount);
    } else {
      int numCountdown = currentCount - count;
      for (int i = 0; i < numCountdown; ++i) {
        phaser.arriveAndDeregister();
      }
    }
  }

  /**
   * 現在のカウンタ値を取得する.
   *
   * @return 現在のカウンタ値
   */
  public int getCount() {
    return getPhaserStatus().numParties();
  }

  /**
   * {@link Phaser} フェーズと登録済みパーティ数を取得する.
   * <pre>
   * 登録済みパーティ数とフェーズを変更する可能性のある操作と排他にすることで,
   * 呼び出した時点でのフェーズに対応する登録済みパーティ数を取得する.
   * </pre>
   *
   * @return {@link Phaser} のフェーズと登録済みパーティ数.
   */
  private synchronized PhaserStatus getPhaserStatus() {
    return new PhaserStatus(phaser.getPhase(), phaser.getRegisteredParties());
  }

  /**
   * フェーズと登録済みパーティ数を格納するレコード.
   *
   * @param phase {@link Phaser} のフェーズ
   * @param numParties {@link Phaser} の登録済みパーティ数
   */
  private record PhaserStatus(int phase, int numParties) {}
}
