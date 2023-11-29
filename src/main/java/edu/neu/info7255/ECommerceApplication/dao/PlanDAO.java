package edu.neu.info7255.ECommerceApplication.dao;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlanDAO {

    private final Jedis jedis;

    @Autowired
    public PlanDAO(Jedis jedis) {
        this.jedis = jedis;
    }

    //Add a field-value pair to the hash with given key
    public void hSet(String key, String field, String value) {
        jedis.hset(key, field, value);
    }

    //Check if the given key exists in the redis db
    public boolean checkIfExists(String key) {
        return jedis.exists(key);
    }

    //Delete the key-value pair
    public long del(String key) {
        return jedis.del(key);
    }

    //Get the value against the key passed
    public String get(String key) {
        return jedis.get(key);
    }

    //Get the value of a field in a Hash with given key
    public String hGet(String key, String field) {
        return jedis.hget(key, field);
    }

    /**
     * @param value2
     * @return
     */
    public Map<String, Map<String, Object>> jsonToMap(JSONObject jsonObject) {
        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> contentMap = new HashMap<>();

        for (Object keyObj : jsonObject.keySet()) {
            String key = keyObj.toString();
            String redisKey = jsonObject.get("objectType") + ":" + jsonObject.get("objectId");
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
                jedis.sadd(redisKey + ":" + key, ((Map<String, Map<String, Object>>) value).entrySet().iterator().next().getKey());
            } else if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
                ((List<Map<String, Map<String, Object>>>) value)
                        .forEach((entry) -> {
                            entry.keySet()
                                    .forEach((listKey) -> {
                                        jedis.sadd(redisKey + ":" + key, listKey);
                                    });
                        });
            } else {
                jedis.hset(redisKey, key, value.toString());
                contentMap.put(key, value);
                map.put(redisKey, contentMap);
            }
        }
        return map;
    }

    public List<Object> jsonToList(JSONArray jsonArray) {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONArray) value = jsonToList((JSONArray) value);
            else if (value instanceof JSONObject) value = jsonToMap((JSONObject) value);
            result.add(value);
        }
        return result;
    }


    private Map<String, Object> getOrDelete(String redisKey, Map<String, Object> resultMap, boolean isDelete) {
        Set<String> keys = jedis.keys(redisKey + ":*");
        keys.add(redisKey);

        for (String key : keys) {
            if (key.equals(redisKey)) {
                if (isDelete) jedis.del(new String[]{key});
                else {
                    Map<String, String> object = jedis.hgetAll(key);
                    for (String attrKey : object.keySet()) {
                        if (!attrKey.equalsIgnoreCase("eTag")) {
                            resultMap.put(attrKey, isInteger(object.get(attrKey)) ? Integer.parseInt(object.get(attrKey)) : object.get(attrKey));
                        }
                    }
                }
            } else {
                String newKey = key.substring((redisKey + ":").length());
                Set<String> members = jedis.smembers(key);
                if (members.size() > 1 || newKey.equals("linkedPlanServices")) {
                    List<Object> listObj = new ArrayList<>();
                    for (String member : members) {
                        if (isDelete) {
                            getOrDelete(member, null, true);
                        } else {
                            Map<String, Object> listMap = new HashMap<>();
                            listObj.add(getOrDelete(member, listMap, false));
                        }
                    }
                    if (isDelete) jedis.del(new String[]{key});
                    else resultMap.put(newKey, listObj);
                } else {
                    if (isDelete) {
                        jedis.del(new String[]{members.iterator().next(), key});
                    } else {
                        Map<String, String> object = jedis.hgetAll(members.iterator().next());
                        Map<String, Object> nestedMap = new HashMap<>();
                        for (String attrKey : object.keySet()) {
                            nestedMap.put(attrKey,
                                    isInteger(object.get(attrKey)) ? Integer.parseInt(object.get(attrKey)) : object.get(attrKey));
                        }
                        resultMap.put(newKey, nestedMap);
                    }
                }
            }
        }
        return resultMap;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
