package top.hazenix.constant;

public class NotifyConstants {
    public static final Long CONFIG_ID = 1L;
    public static final int CONTENT_SNIPPET_LENGTH = 80;
    public static final int TOKEN_BYTE_LENGTH = 32;
    public static final int TOKEN_EXPIRE_DAYS = 7;
    public static final int[] RETRY_DELAY_MINUTES = {1, 5, 30};
    public static final String PASSWORD_MASK = "********";

    private NotifyConstants() {}
}
