package com.easylive.entity.constants;

public class Constants {
    public static final String REDIS_KEY_PREFIX = "easylive:";
    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX+"checkcode:";
    public static final Integer ONE_MIN_MILLS = 60000;
    public static final String PASSWORD_REGEXP = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,18}$";
    public static final String REDIS_KEY_LOGIN_TOKEN = REDIS_KEY_PREFIX+"token:";
    public static final String REDIS_KEY_ADMIN_TOKEN = REDIS_KEY_PREFIX+"admin:token:";
    public static final String REDIS_KEY_CATEGORY_LIST = REDIS_KEY_PREFIX+"category:list:";
}
