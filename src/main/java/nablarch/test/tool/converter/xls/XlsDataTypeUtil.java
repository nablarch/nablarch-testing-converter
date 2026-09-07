package nablarch.test.tool.converter.xls;

import nablarch.test.core.reader.DataType;

/**
 * {@code xls} パッケージ内で共有するデータタイプ判定ユーティリティ。
 */
final class XlsDataTypeUtil {

    private XlsDataTypeUtil() {
    }

    /**
     * 送信同期メッセージ（要求/応答電文 4 種）のデータタイプか判定する。
     *
     * @param type データタイプ
     * @return 送信系 4 種のいずれかなら真
     */
    static boolean isSendSyncType(DataType type) {
        return type == DataType.EXPECTED_REQUEST_HEADER_MESSAGES
                || type == DataType.EXPECTED_REQUEST_BODY_MESSAGES
                || type == DataType.RESPONSE_HEADER_MESSAGES
                || type == DataType.RESPONSE_BODY_MESSAGES;
    }
}
