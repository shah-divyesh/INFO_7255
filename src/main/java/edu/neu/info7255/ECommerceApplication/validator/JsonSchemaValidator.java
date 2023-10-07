package edu.neu.info7255.ECommerceApplication.validator;

import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;

public class JsonSchemaValidator {
    private String jsonPath = "/schema/product_schema.json";

    public void validateSchema(JSONObject data) throws ValidationException {

        try {
            InputStream inputStream = getClass().getResourceAsStream(jsonPath);
            JSONObject schemaJson = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(schemaJson);

            schema.validate(data);
            System.out.println("JSON Schema validation successful!");
        } catch (ValidationException e) {
            
            // You can handle the validation error here or rethrow the exception
            System.out.println("JSON Schema validation unsuccessful!");
            throw new ValidationException("Json Validation failed : " + e.getMessage());
        }
    }
}
