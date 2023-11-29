package edu.neu.info7255.ECommerceApplication.validator;

import java.io.IOException;
import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;

@Service
public class JsonValidator {
     public void validateJson(JSONObject jsonObject) throws ValidationException, IOException {
        try(InputStream inputStream = getClass().getResourceAsStream("/schema//JsonSchema.json")) {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            
            // You can handle the validation error here or rethrow the exception
            System.out.println("JSON Schema validation unsuccessful!");
            throw new ValidationException("Json Validation failed : " + e.getMessage());
        }
    }
}
