package com.chj.tool;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * ToolCallback 包装器，在 Spring AI 的 Reactor 调度线程上执行工具方法时，
 * 恢复 Tomcat 请求线程上的 TransmittableThreadLocal 上下文（如用户认证信息）。
 * <p>
 * 解决 Spring AI 响应式管道中 @Tool 方法调用 ThreadLocalUtil.get() 返回 null 的问题。
 */
public class TtlToolCallbackWrapper implements ToolCallback {

    private final ToolCallback delegate;
    private final Object capturedContext;

    public TtlToolCallbackWrapper(ToolCallback delegate) {
        this.delegate = delegate;
        // 在 Tomcat 请求线程上（ChatController 中构造时）捕获 TTL 上下文快照
        this.capturedContext = TransmittableThreadLocal.Transmitter.capture();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        Object backup = TransmittableThreadLocal.Transmitter.replay(capturedContext);
        try {
            return delegate.call(toolInput);
        } finally {
            TransmittableThreadLocal.Transmitter.restore(backup);
        }
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        Object backup = TransmittableThreadLocal.Transmitter.replay(capturedContext);
        try {
            return delegate.call(toolInput, toolContext);
        } finally {
            TransmittableThreadLocal.Transmitter.restore(backup);
        }
    }
}
