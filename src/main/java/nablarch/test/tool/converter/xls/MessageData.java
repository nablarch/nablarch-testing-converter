package nablarch.test.tool.converter.xls;

import java.util.Map;

import nablarch.test.core.file.FixedLengthFile;

/**
 * メッセージ（{@link nablarch.test.core.reader.DataType#MESSAGE}）の取り出し結果。FW 制御ヘッダと本文を併せ持つ。
 *
 * @see nablarch.test.core.reader.XlsParserAccessor#readMessage(String, String, String)
 */
public final class MessageData {

    /** FW 制御ヘッダ（{@code requestId}／{@code userId} 等。本体が解釈したあとの値） */
    private final Map<String, String> fwHeader;

    /** 本文（固定長ファイルの器。本体が解釈したあとの値） */
    private final FixedLengthFile body;

    /**
     * コンストラクタ。
     *
     * @param fwHeader FW 制御ヘッダ
     * @param body     本文
     */
    public MessageData(Map<String, String> fwHeader, FixedLengthFile body) {
        this.fwHeader = fwHeader;
        this.body = body;
    }

    /** @return FW 制御ヘッダ（本体が解釈したあとの値） */
    public Map<String, String> getFwHeader() {
        return fwHeader;
    }

    /** @return 本文（固定長ファイルの器。本体が解釈したあとの値） */
    public FixedLengthFile getBody() {
        return body;
    }
}
