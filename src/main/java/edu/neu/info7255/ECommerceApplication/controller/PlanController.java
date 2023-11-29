package edu.neu.info7255.ECommerceApplication.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.neu.info7255.ECommerceApplication.service.AuthService;
import edu.neu.info7255.ECommerceApplication.service.PlanService;
import edu.neu.info7255.ECommerceApplication.validator.JsonValidator;

@RestController
@RequestMapping("/plans")
public class PlanController {
    
    private final JsonValidator jsonValidator;
    private final PlanService planService;
    @Autowired
    private AuthService authService;

    @Autowired
    public PlanController(JsonValidator jsonValidator, PlanService planService) {
        this.jsonValidator = jsonValidator;
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<Object> createPlan(@RequestBody String planString, @RequestHeader("Authorization") String idToken) throws URISyntaxException {

        // Checks for the Authorization
        if(!authService.authorize(idToken.substring(7))) {
            return new ResponseEntity<>(new JSONObject().put("message", "Invalid Token").toString(),HttpStatus.UNAUTHORIZED);
        }

        // Check if nothing is passed
        if (planString == null || planString.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("error", "empty request body").toString());
        }

        JSONObject planJson = new JSONObject(planString);

        // JSON SCHEMA VALIDATOR
        try {
            jsonValidator.validateJson(planJson);
        } catch (ValidationException | IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("error", exception.getMessage()).toString());
        }

        // Checks if Plan with same key exist or not
        String planKey = planJson.get("objectType").toString() + "_" + planJson.get("objectId").toString();
        if (planService.checkIfKeyExists(planKey)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new JSONObject().put("error", "plan already exists!").toString());
        }

        //Creates ETAG for Plan
        String eTag = planService.savePlan(planKey, planJson.toString());
        
        return ResponseEntity.created(new URI("/plan/" + planJson.get("objectId").toString())).eTag(eTag).body(new JSONObject().put("message", "plan created successfully!")
                .put("planId", planJson.get("objectId").toString()).toString());

    }

    @GetMapping(value = "/{planId}", produces = "application/json")
    public ResponseEntity<Object> getPlan(
            @RequestHeader HttpHeaders headers,
            @RequestHeader("Authorization") String idToken,
            @PathVariable String planId
    ) {

         // Checks for the Authorization
        if(!authService.authorize(idToken.substring(7))) {
            return new ResponseEntity<>(new JSONObject().put("message", "Invalid Token").toString(),HttpStatus.UNAUTHORIZED);
        }

        String key = "plan_" + planId;
        if (!planService.checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("error", "plan not found!").toString());
        }

        String oldETag = planService.getETag(key);
        String receivedETag = headers.getFirst("If-None-Match");
        if (receivedETag != null && receivedETag.equals(oldETag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(oldETag)
                    .body(new JSONObject().put("message", "plan not modified!").toString());
        }

        String plan = planService.getPlan(key).toString();
        return ResponseEntity.ok().eTag(oldETag).body(plan);
    }

    @DeleteMapping(value = "/{planId}", produces = "application/json")
    public ResponseEntity<Object> deletePlan(@PathVariable String planId, @RequestHeader("Authorization") String idToken) {

         // Checks for the Authorization
        if(!authService.authorize(idToken.substring(7))) {
            return new ResponseEntity<>(new JSONObject().put("message", "Invalid Token").toString(),HttpStatus.UNAUTHORIZED);
        }

        String keyToDelete = "plan_" + planId;
        if (!planService.checkIfKeyExists(keyToDelete)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("error", "plan not found!").toString());
        }

        if (planService.deletePlan(keyToDelete)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().body(new JSONObject().put("error", "internal server error!").toString());
    }

    @PatchMapping(value = "/{planId}")
    public ResponseEntity<Object> updatePlan(@PathVariable String planId, @RequestBody String planUpdates, @RequestHeader("Authorization") String idToken) {
        // Check for authorization
        if (!authService.authorize(idToken.substring(7))) {
            return new ResponseEntity<>(new JSONObject().put("message", "Invalid Token").toString(), HttpStatus.UNAUTHORIZED);
        }
    
        // Check if plan exists
        String key = "plan_" + planId;
        if (!planService.checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("error", "plan not found!").toString());
        }
    
        JSONObject updatesJson = new JSONObject(planUpdates);
        try {
            // Validate the JSON schema of the updates
            jsonValidator.validateJson(updatesJson);
    
            // Apply the updates to the plan
            String eTag = planService.updatePlan(key, updatesJson);
            
            return ResponseEntity.ok().eTag(eTag).body(new JSONObject().put("message", "plan updated successfully!").toString());
        } catch (ValidationException | IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("error", exception.getMessage()).toString());
        }
    }
}
