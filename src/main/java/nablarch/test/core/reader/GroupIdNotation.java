package nablarch.test.core.reader;

/**
 * 上流 API 境界のグループ ID 表記。
 *
 * <p>
 * 中間モデルとアダプタの公開 API が持つグループ ID は<b>生値</b>（{@code case01}、省略時は空文字）である。
 * 半角角括弧で囲む {@code [case01]} は Excel 形式の書式であって値ではない。
 * 中間モデルはテスティングフレームワークの仕様上の意味だけを持つ。
 * </p>
 *
 * <p>
 * 一方、変更できない上流 2 つは API 境界で<b>整形済み</b>のグループ ID を要求する。
 * </p>
 * <ul>
 *   <li>Excel —— {@code GroupDataParsingTemplate} が
 *       {@code getTargetType().getName() + groupId + '='} を組み立ててマーカーセルと前方一致する</li>
 *   <li>YAML —— {@code YamlSection#groupMatches} が
 *       {@code "[" + rawGroupId + "]"} を作って比較する</li>
 * </ul>
 *
 * <p>
 * したがって整形はアダプタの中、<b>上流へ渡す直前</b>の 1 か所に置く。
 * {@code XlsFormatReader}・{@code YamlFormatReader} は {@code [ ]} を扱わない。
 * </p>
 *
 * <p>
 * <b>この表記を知ってよいもう 1 つの層</b>は Excel 版面の読み書きであり、
 * 付けるのが {@code XlsFormatWriter#marker}、外すのが
 * {@code TestCoreReaderAdapter#markerGroupId} である。
 * </p>
 *
 * @author kiyobot
 */
final class GroupIdNotation {

    /** インスタンス化させない。 */
    private GroupIdNotation() {
    }

    /**
     * 生値のグループ ID を上流 API が要求する整形済み表記へ写す。
     *
     * <p>
     * 生値の空文字（および {@code null}）は「グループ指定なし」であり、空文字のまま渡す。
     * </p>
     *
     * @param rawGroupId 生値のグループ ID（省略時は空文字。{@code null} 可）
     * @return 整形済みグループ ID（{@code [case01]}／グループ指定なしは空文字）
     */
    static String format(String rawGroupId) {
        return rawGroupId == null || rawGroupId.isEmpty() ? "" : "[" + rawGroupId + "]";
    }
}
