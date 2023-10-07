package edu.neu.info7255.ECommerceApplication.exceptions;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(Integer id) {
        super("Product with ID " + id + " not found.");
    }
}
