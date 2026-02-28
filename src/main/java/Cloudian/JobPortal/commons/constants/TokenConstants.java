package Cloudian.JobPortal.commons.constants;

import lombok.Data;

public class TokenConstants
{
    public static final Long RESET_PASSWORD_LIVE_TIME = 5 * 60 * 1000L;
    public static final Long ACCESS_LIVE_TIME = 15 * 60 * 1000L;
    public static final Long REFRESH_LIVE_TIME = 10 * 24 * 3600 * 1000L;
    public static final Long VERIFY_EMAIL_LIVE_TIME = 5 * 60 * 1000L;
}
