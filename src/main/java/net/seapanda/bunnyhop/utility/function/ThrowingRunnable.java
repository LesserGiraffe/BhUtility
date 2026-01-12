package net.seapanda.bunnyhop.utility.function;

/**
 * 例外をスローする処理を表す関数型インターフェース.
 *
 * @param <E> スローする例外の型
 * @author K.Koike
 */
@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> extends Runnable {

  void runOrThrow() throws E;

  @Override
  default void run() {
    try {
      runOrThrow();
    } catch (Throwable e) {
      Throwing.sneakyThrow(e);
    }
  }

  /** {@link ThrowingRunnable} を {@link Runnable} に変換する. */
  static <E extends Throwable> Runnable unchecked(ThrowingRunnable<E> runnable) {
    return runnable;
  }
}
