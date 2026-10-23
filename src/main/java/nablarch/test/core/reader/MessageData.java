package nablarch.test.core.reader;

import java.util.Map;

import nablarch.test.core.file.FixedLengthFile;

/**
 * メッセージ（{@link DataType#MESSAGE}）の取り出し結果。FW 制御ヘッダと本文を併せ持つ。
 *
 * @see TestCoreReaderAdapter#readMessage(String, String, String)
 */
public final class MessageData {

    /** FW 制御ヘッダ（{@code requestId}／{@code userId} 等。記法のまま・未加工） */
    private final Map<String, String> fwHeader;

    /** 本文（固定長ファイルの器。記法のまま・未加工） */
    private final FixedLengthFile body;

    /**
     * コンストラクタ。
     *
     * @param fwHeader FW 制御ヘッダ
     * @param body     本文
     */
    MessageData(Map<String, String> fwHeader, FixedLengthFile body) {
        this.fwHeader = fwHeader;
        this.body = body;
    }

    /** @return FW 制御ヘッダ（記法のまま・未加工） */
    public Map<String, String> getFwHeader() {
        return fwHeader;
    }

    /** @return 本文（固定長ファイルの器。記法のまま・未加工） */
    public FixedLengthFile getBody() {
        return body;
    }
}
