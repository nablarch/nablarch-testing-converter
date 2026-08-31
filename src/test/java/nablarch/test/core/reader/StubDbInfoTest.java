package nablarch.test.core.reader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Types;

import org.junit.Test;

/**
 * {@link StubDbInfo} のテストクラス。
 * <p>
 * 本スタブは読み込み（parse→getResult／ビルダ走査）経路の配線にのみ用いる。担保するのは 2 点である
 * ——（1）読み込み経路が実際に呼ぶ {@code getColumnType} が、値加工を起こさないよう型に依存せず
 * {@link Types#VARCHAR} を返すこと。（2）DB 書き込み経路専用の 9 メソッドが、呼ばれたら黙って
 * 既定値を返さず {@link UnsupportedOperationException} で即座に失敗すること。
 * </p>
 *
 * @author kiyobot
 */
public class StubDbInfoTest {

    /** テスト対象。 */
    private final StubDbInfo sut = new StubDbInfo();

    /**
     * Given: 任意のテーブル名・カラム名。
     * When : getColumnType を呼ぶ。
     * Then : テーブル・カラムによらず {@link Types#VARCHAR} が返る。
     */
    @Test
    public void getColumnType_alwaysReturnsVarcharRegardlessOfTableAndColumn() {
        assertThat(sut.getColumnType("USERS", "USER_ID"), is(Types.VARCHAR));
        assertThat(sut.getColumnType("ORDERS", "AMOUNT"), is(Types.VARCHAR));
        assertThat(sut.getColumnType("", ""), is(Types.VARCHAR));
    }

    /**
     * Given: DB 書き込み経路専用の 9 メソッド。
     * When : それぞれを呼ぶ。
     * Then : いずれも {@link UnsupportedOperationException} を送出し、メッセージにメソッド名を含む。
     *
     * <p>担保：番人 9 本が既定値を返して静かに通り抜けないこと。</p>
     */
    @Test
    public void writePathMethods_allThrowUnsupportedOperationNamingTheMethod() {
        assertNotOnReadPath("getPrimaryKeys", new Call() {
            public void run() {
                sut.getPrimaryKeys("USERS");
            }
        });
        assertNotOnReadPath("getColumns", new Call() {
            public void run() {
                sut.getColumns("USERS");
            }
        });
        assertNotOnReadPath("isUniqueIndex", new Call() {
            public void run() {
                sut.isUniqueIndex("USERS", "USER_ID");
            }
        });
        assertNotOnReadPath("getColumnLength", new Call() {
            public void run() {
                sut.getColumnLength("USERS", "USER_ID");
            }
        });
        assertNotOnReadPath("isComputedColumn", new Call() {
            public void run() {
                sut.isComputedColumn("USERS", "USER_ID");
            }
        });
        assertNotOnReadPath("isNumberTypeColumn", new Call() {
            public void run() {
                sut.isNumberTypeColumn("USERS", "USER_ID");
            }
        });
        assertNotOnReadPath("isDateTypeColumn", new Call() {
            public void run() {
                sut.isDateTypeColumn("USERS", "USER_ID");
            }
        });
        assertNotOnReadPath("isBinaryTypeColumn", new Call() {
            public void run() {
                sut.isBinaryTypeColumn("USERS", "USER_ID");
            }
        });
        assertNotOnReadPath("isBooleanTypeColumn", new Call() {
            public void run() {
                sut.isBooleanTypeColumn("USERS", "USER_ID");
            }
        });
    }

    /** 呼び出しを包む。 */
    private interface Call {
        /** 対象メソッドを呼ぶ。 */
        void run();
    }

    /**
     * 呼び出しが読み込み経路外として弾かれることを検証する。
     *
     * @param method 期待するメソッド名（例外メッセージに含まれること）
     * @param call   呼び出し
     */
    private static void assertNotOnReadPath(String method, Call call) {
        try {
            call.run();
            fail("should throw UnsupportedOperationException: " + method);
        } catch (UnsupportedOperationException e) {
            assertThat(method + " のメッセージ", e.getMessage(),
                    is("DbInfo#" + method + " must not be called on the DB-less converter read path."));
        }
    }
}
