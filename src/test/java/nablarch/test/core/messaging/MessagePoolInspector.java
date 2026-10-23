package nablarch.test.core.messaging;

import nablarch.test.core.file.FixedLengthFile;

/**
 * {@link MessagePool} が protected で隠している元データへ、テストから到達するための相乗りヘルパ。
 *
 * <p>
 * 送信同期電文の値をフレームワーク本体に読ませて「正解」として取り出すとき、本体
 * {@code GroupMessageParser} が返すのは {@link MessagePool} 群であり、元の固定長ファイルは
 * {@code getSource()}（protected）の向こうにある。protected は同一パッケージからは見えるため、
 * 本クラスを {@link MessagePool} と同じパッケージへ 1 枚だけ置いて壁を越える。
 * </p>
 *
 * <p>
 * 相乗りの影響は本クラスに閉じる。テスト専用であり、{@code src/main} からは使わない。
 * </p>
 */
public final class MessagePoolInspector {

    /** ユーティリティクラスのため生成させない */
    private MessagePoolInspector() {
    }

    /**
     * メッセージプールが持つ元の固定長ファイルを取り出す。
     *
     * @param pool メッセージプール
     * @return 元の固定長ファイル
     */
    public static FixedLengthFile sourceOf(MessagePool pool) {
        return pool.getSource();
    }
}
