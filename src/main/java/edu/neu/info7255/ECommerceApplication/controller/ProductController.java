package edu.neu.info7255.ECommerceApplication.controller;

import org.everit.json.schema.ValidationException;
import org.json.HTTP;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.neu.info7255.ECommerceApplication.exceptions.ProductNotFoundException;
import edu.neu.info7255.ECommerceApplication.model.Product;
import edu.neu.info7255.ECommerceApplication.service.ProductService;
import edu.neu.info7255.ECommerceApplication.validator.JsonSchemaValidator;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    private JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();   

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Object> addProduct(@RequestBody Product p){
        JSONObject productJson = new JSONObject(p); // Convert the product to JSONObject

        Product existingProduct = productService.findById(p.getProductId());
        if (existingProduct != null) {
            return new ResponseEntity<>(new JSONObject().put("message", "Product with ID " + p.getProductId() + " already exists.").toString(), HttpStatus.CONFLICT);
        }

        try {
            Product savedProduct = productService.save(p);
            jsonSchemaValidator.validateSchema(productJson);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (ValidationException e) {
            return new ResponseEntity<>(new JSONObject().put("message", "Validation failed: " + e.getMessage()).toString(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new JSONObject().put("message", "An error occurred: " + e.getMessage()).toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProduct(@PathVariable Integer id, HttpServletRequest request ) {

        try{
            Product product = productService.findById(id);
            if(product == null){
                throw new ProductNotFoundException(id);
            }

            String productETag = Integer.toHexString(product.hashCode());
            String requestETag = request.getHeader("If-None-Match");

            // System.out.println(requestETag +"---"+ productETag);

            if (requestETag != null && requestETag.equals(productETag)) {
                // ETags match, return 304 Not Modified status
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
    
            // Return product as usual along with ETag header
            return ResponseEntity.ok().header("ETag", productETag).body(product);

        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(new JSONObject().put("message", e.getMessage()).toString(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllProducts() {

        try{
            Iterable<Product> products = productService.findAll();
            if(!products.iterator().hasNext()){z
                return new ResponseEntity<>(new JSONObject().put("message", "No Products Found").toString(), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(products, HttpStatus.OK);
        }catch (Exception e) {
            // Handle other exceptions here as necessary
            return new ResponseEntity<>(new JSONObject().put("message", "An error occurred: " + e.getMessage()).toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable Integer id) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                throw new ProductNotFoundException(id);
            }
            productService.delete(id);
            return new ResponseEntity<>(new JSONObject().put("message", "Product deleted successfully.").toString(), HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(new JSONObject().put("message", "No Product with id=" +id+ "  found.").toString(), HttpStatus.NOT_FOUND);
        }
    }
}
