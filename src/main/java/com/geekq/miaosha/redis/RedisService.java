package com.geekq.miaosha.redis;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

	@Autowired
	private RedisTemplate<String,String> redisTemplate;

	/**
	 * 设置失效时间
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean setnx(String key ,String value){

		return redisTemplate.opsForValue().setIfAbsent(key, value);
//		Jedis jedis =null;
//		Long result = null;
//		try {
//			jedis = jedisPool.getResource();
//			result = jedis.setnx(key,value);
//		}catch (Exception e){
//			log.error("expire key:{} error",key,e);
//		jedisPool.returnResource(jedis);
//		return  result;
//		}
//		jedisPool.returnResource(jedis);
//		return  result;

	}
	/**
	 * 设置key的有效期，单位是秒
	 * @param key
	 * @param exTime
	 * @return
	 */
	public Boolean expire(String key,int exTime){
		return redisTemplate.expire(key, (long) exTime, TimeUnit.MILLISECONDS);
//		Jedis jedis = null;
//		Long result = null;
//		try {
//			jedis =  jedisPool.getResource();
//			result = jedis.expire(key,exTime);
//		} catch (Exception e) {
//			log.error("expire key:{} error",key,e);
//			jedisPool.returnBrokenResource(jedis);
//			return result;
//		}
//		jedisPool.returnResource(jedis);
//		return result;
	}

	/**
	 * 获取当个对象
	 * */
	public <T> T get(KeyPrefix prefix, String key,  Class<T> clazz) {
		String realKey  = prefix.getPrefix() + key;
		return stringToBean(redisTemplate.opsForValue().get(realKey), clazz);
//		 Jedis jedis = null;
//		 try {
//			 jedis =  jedisPool.getResource();
//			 //生成真正的key
//			 String realKey  = prefix.getPrefix() + key;
//			 String  str = jedis.get(realKey);
//			 T t =  stringToBean(str, clazz);
//			 return t;
//		 }finally {
//			  returnToPool(jedis);
//		 }
	}

    public  String get(String key){
		return redisTemplate.opsForValue().get(key);
//        Jedis jedis = null;
//        String result = null;
//        try {
//            jedis =  jedisPool.getResource();
//            result = jedis.get(key);
//        } catch (Exception e) {
//            log.error("expire key:{} error",key,e);
//            jedisPool.returnBrokenResource(jedis);
//            return result;
//        }
//        jedisPool.returnResource(jedis);
//        return result;
    }


    public  String getset(String key,String value){
		return redisTemplate.opsForValue().getAndSet(key, value);
//        Jedis jedis = null;
//        String result = null;
//        try {
//            jedis =  jedisPool.getResource();
//            result = jedis.getSet(key,value);
//        } catch (Exception e) {
//            log.error("expire key:{} error",key,e);
//            jedisPool.returnBrokenResource(jedis);
//            return result;
//        }
//        jedisPool.returnResource(jedis);
//        return result;
    }
	/**
	 * 设置对象
	 * */
	public <T> void set(KeyPrefix prefix, String key,  T value) {
		String str = beanToString(value);
		String realKey  = prefix.getPrefix() + key;
		int seconds =  prefix.expireSeconds();
		if(seconds <= 0) {
			redisTemplate.opsForValue().set(realKey, str);
		}else {
			redisTemplate.opsForValue().set(realKey, str, seconds, TimeUnit.SECONDS);
		}
//		 Jedis jedis = null;
//		 try {
//			 jedis =  jedisPool.getResource();
//			 String str = beanToString(value);
//			 if(str == null || str.length() <= 0) {
//				 return false;
//			 }
//			//生成真正的key
//			 String realKey  = prefix.getPrefix() + key;
//			 int seconds =  prefix.expireSeconds();
//			 if(seconds <= 0) {
//				 jedis.set(realKey, str);
//			 }else {
//				 jedis.setex(realKey, seconds, str);
//			 }
//			 return true;
//		 }finally {
//			  returnToPool(jedis);
//		 }
	}
	
	/**
	 * 判断key是否存在
	 * */
	public boolean exists(KeyPrefix prefix, String key) {
		String realKey  = prefix.getPrefix() + key;
		return redisTemplate.hasKey(realKey);
//		 Jedis jedis = null;
//		 try {
//			 jedis =  jedisPool.getResource();
//			//生成真正的key
//			 String realKey  = prefix.getPrefix() + key;
//			return  jedis.exists(realKey);
//		 }finally {
//			  returnToPool(jedis);
//		 }
	}
	
	/**
	 * 删除
	 * */
	public boolean delete(KeyPrefix prefix, String key) {
		String realKey  = prefix.getPrefix() + key;
		if (redisTemplate.hasKey(realKey))
			return redisTemplate.delete(realKey);
		return false;
//		 Jedis jedis = null;
//		 try {
//			 jedis =  jedisPool.getResource();
//			//生成真正的key
//			String realKey  = prefix.getPrefix() + key;
//			long ret =  jedis.del(realKey);
//			return ret > 0;
//		 }finally {
//			  returnToPool(jedis);
//		 }
	}
	
	/**
	 * 增加值
	 * */
	public Long incr(KeyPrefix prefix, String key) {
		String realKey  = prefix.getPrefix() + key;
		return redisTemplate.opsForValue().increment(realKey);
//		 Jedis jedis = null;
//		 try {
//			 jedis =  jedisPool.getResource();
//			//生成真正的key
//			 String realKey  = prefix.getPrefix() + key;
//			return  jedis.incr(realKey);
//		 }finally {
//			  returnToPool(jedis);
//		 }
	}
	
	/**
	 * 减少值
	 * */
	public <T> Long decr(KeyPrefix prefix, String key) {
		String realKey  = prefix.getPrefix() + key;
		return redisTemplate.opsForValue().decrement(realKey);
//		 Jedis jedis = null;
//		 try {
//			 jedis =  jedisPool.getResource();
//			//生成真正的key
//			 String realKey  = prefix.getPrefix() + key;
//			return  jedis.decr(realKey);
//		 }finally {
//			  returnToPool(jedis);
//		 }
	}

    public boolean del(String key){
		if (redisTemplate.hasKey(key))
			return redisTemplate.delete(key);
		return false;
//        Jedis jedis = null;
//        Long result = null;
//        try {
//            jedis =  jedisPool.getResource();
//            result = jedis.del(key);
//        } catch (Exception e) {
//            log.error("del key:{} error",key,e);
//            jedisPool.returnBrokenResource(jedis);
//            return result;
//        }
//        jedisPool.returnResource(jedis);
//        return result;
    }


	public long delete(KeyPrefix prefix) {
		if(prefix == null) {
			return 0;
		}
		String keys = "*"+prefix.getPrefix()+"*";
		return redisTemplate.delete(redisTemplate.keys(keys));

//		Jedis jedis = null;
//		try {
//			jedis = jedisPool.getResource();
//			jedis.del(keys.toArray(new String[0]));
//			return true;
//		} catch (final Exception e) {
//			e.printStackTrace();
//			return false;
//		} finally {
//			if(jedis != null) {
//				jedis.close();
//			}
//		}
	}
	
//	public List<String> scanKeys(String key) {
//		redisTemplate.opsForValue().multiGet()
//		Jedis jedis = null;
//		try {
//			jedis = jedisPool.getResource();
//			List<String> keys = new ArrayList<String>();
//			String cursor = "0";
//			ScanParams sp = new ScanParams();
//			sp.match("*"+key+"*");
//			sp.count(100);
//			do{
//				ScanResult<String> ret = jedis.scan(cursor, sp);
//				List<String> result = ret.getResult();
//				if(result!=null && result.size() > 0){
//					keys.addAll(result);
//				}
//				//再处理cursor
//				cursor = ret.getStringCursor();
//			}while(!cursor.equals("0"));
//			return keys;
//		} finally {
//			if (jedis != null) {
//				jedis.close();
//			}
//		}
//	}
	
	public static <T> String beanToString(T value) {
		if(value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if(clazz == int.class || clazz == Integer.class) {
			 return ""+value;
		}else if(clazz == String.class) {
			 return (String)value;
		}else if(clazz == long.class || clazz == Long.class) {
			return ""+value;
		}else {
			return JSON.toJSONString(value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T stringToBean(String str, Class<T> clazz) {
		if(str == null || str.length() <= 0 || clazz == null) {
			 return null;
		}
		if(clazz == int.class || clazz == Integer.class) {
			 return (T)Integer.valueOf(str);
		}else if(clazz == String.class) {
			 return (T)str;
		}else if(clazz == long.class || clazz == Long.class) {
			return  (T)Long.valueOf(str);
		}else {
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
	}

//	private void returnToPool(Jedis jedis) {
//		 if(jedis != null) {
//			 jedis.close();
//		 }
//	}

}
