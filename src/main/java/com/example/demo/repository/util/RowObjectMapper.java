package com.example.demo.repository.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class RowObjectMapper {

    private static final Map<Class<?>, Function<Object, ?>> CONVERTERS = new HashMap<>();

    static {
        CONVERTERS.put(UUID.class, value -> {
            if (value instanceof String strValue) {
                return UUID.fromString(strValue);
            } else if (value instanceof UUID uuidValue) {
                return uuidValue;
            } else {
                throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to UUID");
            }
        });
        CONVERTERS.put(LocalDateTime.class, value -> value instanceof java.time.OffsetDateTime ? ((java.time.OffsetDateTime) value).toLocalDateTime() : (LocalDateTime) value);
        CONVERTERS.put(LocalDate.class, value -> value instanceof LocalDateTime ? ((LocalDateTime) value).toLocalDate() : value);
        CONVERTERS.put(BigDecimal.class, value -> new BigDecimal(value.toString()));
        CONVERTERS.put(String.class, Object::toString);
        CONVERTERS.put(Integer.class, value -> ((Number) value).intValue());
        CONVERTERS.put(Long.class, value -> ((Number) value).longValue());
        CONVERTERS.put(Double.class, value -> ((Number) value).doubleValue());
        CONVERTERS.put(Float.class, value -> ((Number) value).floatValue());
        CONVERTERS.put(Boolean.class, value -> (Boolean) value);
        CONVERTERS.put(Enum.class, value -> {
            try {
                Class<?> enumClass = Class.forName(((Enum<?>) value).getClass().getName());
                Method valueOfMethod = enumClass.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, value.toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("Error converting String to Enum: " + value, e);
            }
        });
    }

    public static <T> T apply(Map<String, Object> row, Class<T> targetClass, String prefix) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            Field[] fields = targetClass.getDeclaredFields();

            for (Field field : fields) {
                String columnName = prefix.concat("_"+camelToSnake(field.getName()));
                try {
                    Object value = row.get(columnName);
                    if (value != null) {
                        setFieldValue(instance, field, value);
                    }
                } catch (IllegalArgumentException e) {
                    // Column not found, continue to the next field
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping row to object", e);
        }
    }

    private static <T> void setFieldValue(T instance, Field field, Object value) throws Exception {
        String setterName = "set" + capitalize(field.getName());
        Method setter = findSetter(instance.getClass(), setterName, field.getType());

        if (setter == null) {
            throw new NoSuchMethodException("No setter method found for field: " + field.getName());
        } else {
            setter.invoke(instance, convertValue(value, field.getType()));
        }
    }

    private static Method findSetter(Class<?> clazz, String setterName, Class<?> parameterType) {
        try {
            return clazz.getMethod(setterName, parameterType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        Function<Object, ?> converter = CONVERTERS.get(targetType);
        if (converter != null) {
            return converter.apply(value);
        } else if (targetType.isEnum()) {
            try {
                Method valueOfMethod = targetType.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, value.toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("Error converting String to Enum: " + value, e);
            }
        } else {
            return value; // Default case
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder snakeCase = new StringBuilder();
        snakeCase.append(Character.toLowerCase(camelCase.charAt(0)));

        for (int i = 1; i < camelCase.length(); i++) {
            char currentChar = camelCase.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                snakeCase.append('_').append(Character.toLowerCase(currentChar));
            } else {
                snakeCase.append(currentChar);
            }
        }
        return snakeCase.toString();
    }
}