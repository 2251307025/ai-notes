package com.chj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Token 生命周期配置。
 * <ul>
 *   <li>{@code idleTimeoutMinutes} — Redis TTL，每次有效请求重置（滑动窗口）</li>
 *   <li>{@code absoluteExpiryHours} — JWT exp，会话绝对最大存活时间（安全上限）</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.token")
public class TokenProperties {
    private long idleTimeoutMinutes = 60;
    private long absoluteExpiryHours = 8;
}
