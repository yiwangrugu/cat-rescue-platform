package com.catrescue.catrescueplatform.config;

import com.catrescue.catrescueplatform.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class BusinessLogAspect {

    private final SystemLogService systemLogService;

    private static final ThreadLocal<Boolean> IS_LOGGING = ThreadLocal.withInitial(() -> false);

    @Around("@annotation(businessLog)")
    public Object logBusinessOperation(ProceedingJoinPoint joinPoint, BusinessLog businessLog) throws Throwable {
        if (IS_LOGGING.get()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                IS_LOGGING.set(true);
                String description = buildDescription(businessLog, joinPoint, result, exception);
                HttpServletRequest request = getCurrentHttpRequest();
                systemLogService.log(businessLog.action(), businessLog.module(), description, request);
            } finally {
                IS_LOGGING.remove();
            }
        }
    }

    private String buildDescription(BusinessLog businessLog, ProceedingJoinPoint joinPoint, Object result,
            Exception exception) {
        StringBuilder sb = new StringBuilder();

        String template = businessLog.description();
        if (!template.isEmpty()) {
            sb.append(replacePlaceholders(template, joinPoint, result));
        } else {
            sb.append(businessLog.action());
        }

        if (exception != null) {
            sb.append(" - 失败");
            String msg = exception.getMessage();
            if (msg != null && msg.length() > 100) {
                msg = msg.substring(0, 100) + "...";
            }
            if (msg != null) {
                sb.append(" - ").append(msg);
            }
        } else {
            sb.append(" - 成功");
        }

        return sb.toString();
    }

    private String replacePlaceholders(String template, ProceedingJoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String resultStr = template;

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    String value = getValueAsString(args[i]);

                    if (paramNames != null && paramNames.length > i) {
                        String paramPlaceholder = "{" + paramNames[i] + "}";
                        if (resultStr.contains(paramPlaceholder)) {
                            resultStr = resultStr.replace(paramPlaceholder, value);
                        }

                        // 处理嵌套占位符格式：{param.field}
                        String nestedParamPlaceholder = "{" + paramNames[i] + ".";
                        if (resultStr.contains(nestedParamPlaceholder)) {
                            resultStr = replaceNestedPlaceholders(resultStr, paramNames[i], args[i]);
                        }
                    }

                    try {
                        java.lang.reflect.Field[] fields = args[i].getClass().getDeclaredFields();
                        for (java.lang.reflect.Field field : fields) {
                            String fieldPlaceholder = "{" + field.getName() + "}";
                            if (resultStr.contains(fieldPlaceholder)) {
                                field.setAccessible(true);
                                Object fieldValue = field.get(args[i]);
                                if (fieldValue != null) {
                                    resultStr = resultStr.replace(fieldPlaceholder, fieldValue.toString());
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (result != null) {
            String resultPlaceholder = "{result}";
            if (resultStr.contains(resultPlaceholder)) {
                String value = getValueAsString(result);
                resultStr = resultStr.replace(resultPlaceholder, value);
            }

            try {
                java.lang.reflect.Field[] fields = result.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    String fieldPlaceholder = "{" + field.getName() + "}";
                    if (resultStr.contains(fieldPlaceholder)) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(result);
                        if (fieldValue != null) {
                            resultStr = resultStr.replace(fieldPlaceholder, fieldValue.toString());
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        return resultStr;
    }

    private String replaceNestedPlaceholders(String template, String paramName, Object paramValue) {
        String resultStr = template;

        if (paramValue == null) {
            return resultStr;
        }

        // 使用正则表达式匹配所有 {param.field} 格式的占位符
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{" + paramName + "\\.(.*?)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(resultStr);

        while (matcher.find()) {
            String fullPlaceholder = matcher.group(0); // 完整的占位符，如 {post.title}
            String fieldPath = matcher.group(1); // 字段路径，如 title

            try {
                Object fieldValue = getNestedFieldValue(paramValue, fieldPath);
                if (fieldValue != null) {
                    resultStr = resultStr.replace(fullPlaceholder, fieldValue.toString());
                }
            } catch (Exception e) {
                // 如果获取字段值失败，保留原始占位符
            }
        }

        return resultStr;
    }

    private Object getNestedFieldValue(Object obj, String fieldPath) throws Exception {
        String[] fieldNames = fieldPath.split("\\.");
        Object current = obj;

        for (String fieldName : fieldNames) {
            if (current == null) {
                return null;
            }

            java.lang.reflect.Field field = current.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            current = field.get(current);
        }

        return current;
    }

    private String getValueAsString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Number) {
            return obj.toString();
        }

        // 尝试使用反射获取常见属性
        String[] fieldNames = { "title", "name", "username", "content", "description" };
        for (String fieldName : fieldNames) {
            try {
                java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    return fieldValue.toString();
                }
            } catch (Exception e) {
                // 忽略字段不存在的异常
            }
        }

        // 如果上述字段都不存在或为空，尝试获取id
        try {
            java.lang.reflect.Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(obj);
            if (idValue != null) {
                return "ID: " + idValue.toString();
            }
        } catch (Exception e) {
        }

        // 最后尝试toString
        return obj.toString();
    }

    private javax.servlet.http.HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                return (javax.servlet.http.HttpServletRequest) attributes.getRequest();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
