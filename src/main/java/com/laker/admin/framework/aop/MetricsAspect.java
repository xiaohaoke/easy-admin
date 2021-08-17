package com.laker.admin.framework.aop;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laker.admin.module.ext.entity.ExtLog;
import com.laker.admin.module.ext.service.IExtLogService;
import com.laker.admin.utils.http.HttpServletRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Bean的优先级设置为最高
 */
@Aspect
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MetricsAspect {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    IExtLogService extLogService;

    @Pointcut("@annotation(Metrics) || @within(Metrics)")
    public void withAnnotationMetrics() {
    }

    @Around("withAnnotationMetrics()")
    public Object metrics(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String name = signature.toShortString();
        Object returnValue;
        Instant start = Instant.now();
        ExtLog logBean = new ExtLog();
        logBean.setIp(HttpServletRequestUtil.getRemoteAddress());
        logBean.setUri(HttpServletRequestUtil.getRequestURI());
        logBean.setUserId(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        logBean.setClient(HttpServletRequestUtil.getRequestUserAgent());
        logBean.setRequest(objectMapper.writeValueAsString(pjp.getArgs()));
        logBean.setMethod(name);
        logBean.setStatus(true);
        try {
            returnValue = pjp.proceed();
        } catch (Exception ex) {
            log.info("method:{},fail,cost:{}ms,uri:{},param:{}", name, Duration.between(start, Instant.now()).toMillis(), HttpServletRequestUtil.getRequestURI(), objectMapper.writeValueAsString(pjp.getArgs()));
            logBean.setCost((int) Duration.between(start, Instant.now()).toMillis());
            logBean.setCreateTime(LocalDateTime.now());
            logBean.setStatus(false);
            extLogService.save(logBean);
            log.error(name, ex);
            throw ex;

        }
        String response = objectMapper.writeValueAsString(returnValue);
        log.info("method:{},success,cost:{}ms,uri:{},param:{},return:{}", name, Duration.between(start, Instant.now()).toMillis(), HttpServletRequestUtil.getRequestURI(), objectMapper.writeValueAsString(pjp.getArgs()), response);
        logBean.setCost((int) Duration.between(start, Instant.now()).toMillis());
        logBean.setCreateTime(LocalDateTime.now());
        if (StrUtil.isNotBlank(response) && response.length() <= 500) {
            logBean.setResponse(response);
        }
        extLogService.save(logBean);
        return returnValue;
    }
}
