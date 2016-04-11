package com.longcoding.undefined.models.ehcache;

import lombok.EqualsAndHashCode;
import scala.App;

import java.io.Serializable;

/**
 * Created by longcoding on 16. 4. 8..
 */
@EqualsAndHashCode
public class AppInfoCache implements Serializable, Cloneable {

    private static final long serialVersionUID = 1532927748257139491L;

    private String appId;
    private String appKey;
    private String appName;

    private int dailyRateLimit;
    private int minutelyRateLimit;

    public AppInfoCache(String appId, String appKey, String appName, int dailyRateLimit, int minutelyRateLimit) {
        this.appId = appId;
        this.appKey = appKey;
        this.appName = appName;
        this.dailyRateLimit = dailyRateLimit;
        this.minutelyRateLimit = minutelyRateLimit;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getDailyRateLimit() {
        return dailyRateLimit;
    }

    public void setDailyRateLimit(int dailyRateLimit) {
        this.dailyRateLimit = dailyRateLimit;
    }

    public int getMinutelyRateLimit() {
        return minutelyRateLimit;
    }

    public void setMinutelyRateLimit(int minutelyRateLimit) {
        this.minutelyRateLimit = minutelyRateLimit;
    }
}