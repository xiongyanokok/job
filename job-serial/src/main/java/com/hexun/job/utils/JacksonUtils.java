package com.hexun.job.utils;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * jackson 序列化工具类
 * 
 * @author xiongyan
 * @date 2017年5月17日 上午10:14:24
 */
public class JacksonUtils {

	/**
	 * ObjectMapper
	 */
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * 序列化
	 * 
	 * @param value
	 * @return
	 */
	public static String serialize(Object value) {
        if (null == value) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            return null;
        }
    }
	
	/**
	 * 反序列化
	 * 
	 * @param content
	 * @param valueType
	 * @return
	 */
	public <T> T deserialize(String content, Class<T> valueType) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            return null;
        }
    }
	
	/**
	 * 反序列化
	 * 
	 * @param content
	 * @param valueTypeRef
	 * @return
	 */
	public static <T> T deserialize(String content, TypeReference<T> valueTypeRef) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            return objectMapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            return null;
        }
    }
}
