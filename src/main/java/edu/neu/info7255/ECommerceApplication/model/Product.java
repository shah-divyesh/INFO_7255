package edu.neu.info7255.ECommerceApplication.model;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

// import jakarta.annotation.Generated;
@RedisHash("Product")
public class Product implements Serializable{
    @Id
    private Integer productId;

    private String productName;
    
    private double productPrice;
    
    private String productDescription;

    public int getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    public String toString() {
        return "Product{" +
        "Id=" + productId +
        ", Name='" + productName + '\'' +
        ", Price='" + productPrice + '\'' +
        ", Description='" + productDescription + '\'' +
        '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, productPrice);
    }

    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Product product = (Product) obj;

        return Objects.equals(productId, product.productId) &&
               Objects.equals(productName, product.productName) &&
               Objects.equals(productPrice, product.productPrice);
    }
}

