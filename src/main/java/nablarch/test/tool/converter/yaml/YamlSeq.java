package nablarch.test.tool.converter.yaml;

/**
 * YAML のシーケンス項目（マップ）を「先頭プロパティを {@code - } 行に載せ、以降を字下げ」して出力する補助。
 * <p>
 * 1 項目は自身のレベル {@code level} を持ち、{@code - } は {@code ind(level)} に、各プロパティの内容は
 * {@code ind(level+1)} に揃う。
 * </p>
 */
class YamlSeq {

    /** 出力先。 */
    private final StringBuilder sb;

    /** 項目のレベル（{@code - } の字下げ）。 */
    private final int level;

    /** 先頭行（{@code - } を載せる行）が未出力なら真。 */
    private boolean dashPending = true;

    /**
     * @param sb    出力先
     * @param level 項目のレベル
     */
    YamlSeq(StringBuilder sb, int level) {
        this.sb = sb;
        this.level = level;
    }

    /**
     * {@code key: value} のプロパティ行を出力する。
     *
     * @param k キー
     * @param v 値（クォートして出力。{@code null} は {@code null}）
     */
    void prop(String k, String v) {
        line(YamlFormatWriter.key(k) + ": " + YamlFormatWriter.q(v));
    }

    /**
     * {@code key:}（値は後続の字下げで続く）のヘッダ行を出力する。
     *
     * @param k キー
     */
    void header(String k) {
        line(YamlFormatWriter.key(k) + ":");
    }

    /**
     * この項目のプロパティ配下に置く子シーケンス（{@code - } 行）のレベルを返す。
     * <p>
     * プロパティ本体は {@code level + 1} に出るので、その子シーケンス項目の {@code - } は
     * さらに 1 段下げて {@code level + 2} に揃える。
     * </p>
     *
     * @return 子シーケンスのレベル
     */
    int childLevel() {
        return level + 2;
    }

    /**
     * 1 行を出力する。先頭行なら {@code - } を載せ、以降は {@code ind(level+1)} に揃える。
     *
     * @param content 行の内容
     */
    void line(String content) {
        if (dashPending) {
            sb.append(YamlFormatWriter.ind(level)).append("- ").append(content).append('\n');
            dashPending = false;
        } else {
            sb.append(YamlFormatWriter.ind(level + 1)).append(content).append('\n');
        }
    }
}
