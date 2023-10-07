package edu.neu.info7255.ECommerceApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.neu.info7255.ECommerceApplication.dao.ProductDAO;
import edu.neu.info7255.ECommerceApplication.model.Product;

@Service
public class ProductService {

    private ProductDAO productDAO;

    @Autowired
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product save(Product product) {
        // Business logic (if any) can be added here
        return productDAO.save(product);
    }

    public Product findById(Integer id) {
        // Business logic (if any) can be added here
        return productDAO.findById(id);
    }

    public Iterable<Product> findAll() {
        // Business logic (if any) can be added here
        return productDAO.findAll();
    }

    public void delete(Integer id) {
        // Business logic (if any) can be added here
        productDAO.delete(id);
    }

    public boolean isIdExists(String id){
        return productDAO.checkIfIdExist(id);
    }
}
