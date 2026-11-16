package nablarch.test.tool.converter.reader;

/**
 * グループ ID の書き方の決まり。
 *
 * <p>
 * 変換ツールが扱うグループ ID（中間モデルの {@code groupId} など）は、半角角括弧 {@code [ ]} を付けない値
 * （{@code case01}。省略時は空文字）である。{@code [case01]} のように {@code [ ]} で囲むのは Excel 形式の
 * 書き方であって、値の一部ではない。
 * </p>
 *
 * <p>
 * 本体の API（Excel 形式のパーサ、YAML 形式のビルダ）は {@code [ ]} で囲んだグループ ID を受け取るため、
 * 本体を呼ぶ直前に {@link #format} で囲む。Excel 形式へ書き出すときも {@link #format} で囲む。
 * </p>
 *
 * @author kiyobot
 */
public final class GroupIdNotation {

    /** インスタンス化させない。 */
    private GroupIdNotation() {
    }

    /**
     * グループ ID を {@code [ ]} で囲む。
     *
     * @param rawGroupId {@code [ ]} を付けないグループ ID（省略時は空文字。{@code null} 可）
     * @return {@code [ ]} で囲んだグループ ID（{@code [case01]}）。省略時は空文字
     */
    public static String format(String rawGroupId) {
        return rawGroupId == null || rawGroupId.isEmpty() ? "" : "[" + rawGroupId + "]";
    }
}
