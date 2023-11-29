package edu.neu.info7255.ECommerceApplication.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import edu.neu.info7255.ECommerceApplication.dao.PlanDAO;

@Service
public class PlanService {
    private final PlanDAO planDao;

    @Autowired
    public PlanService(PlanDAO planDao) {
        this.planDao = planDao;
    }

    public boolean checkIfKeyExists(String key) {
        return planDao.checkIfExists(key);
    }

    public String getETag(String key) {
        return planDao.hGet(key, "eTag");
    }

    public String savePlan(String key, String planJsonString) {
        String newETag =  DigestUtils.md5Hex(planJsonString);
        planDao.hSet(key, key, planJsonString);
        planDao.hSet(key, "eTag", newETag);
        return newETag;
    }

    // public String savePlan(String key, JSONObject planJson) {
    //     String newETag =  DigestUtils.md5Hex(planJson.toString());
    //     jsonToMap(planJson);
    //     planDao.hSet(key, "eTag", newETag);
    //     return newETag;
    // }

    public JSONObject getPlan(String key) {
        String planString = planDao.hGet(key, key);
        return new JSONObject(planString);
    }

    public boolean deletePlan(String key) {
        return planDao.del(key) == 1;
    }

    public String updatePlan(String key, JSONObject updatedPlan) {
        // Fetch the existing plan
        String existingPlanString = planDao.hGet(key, key);
        JSONObject existingPlan = new JSONObject(existingPlanString);

        // Update the existing plan with new values from updatedPlan
        for (Object obj : updatedPlan.keySet()) {
            String updateKey = obj.toString();
            existingPlan.put(updateKey, updatedPlan.get(updateKey));
        }

        // Convert the updated plan back to a string
        String updatedPlanString = existingPlan.toString();

        // Generate a new ETag
        String newETag = DigestUtils.md5Hex(updatedPlanString);

        // Save the updated plan and the new ETag in the data store
        planDao.hSet(key, key, updatedPlanString);
        planDao.hSet(key, "eTag", newETag);

        // Return the new ETag
        return newETag;
    }

    // public Map<String, Map<String, Object>> jsonToMap(JSONObject jsonObject) {
    //      return planDao.jsonToMap(jsonObject);
    // }

    // private Map<String, Object> getOrDelete(String redisKey, Map<String, Object> resultMap, boolean isDelete) {
    //     return planDao.getOrDelete(redisKey, resultMap, isDelete);
    // }
    
}
