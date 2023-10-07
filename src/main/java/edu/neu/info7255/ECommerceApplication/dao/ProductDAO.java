package edu.neu.info7255.ECommerceApplication.dao;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import edu.neu.info7255.ECommerceApplication.model.Product;
// import edu.neu.info7255.ECommerceApplication.repository.ProductRepository;
import redis.clients.jedis.Jedis;

// @Repository
// public class ProductDAO {
//     private final ProductRepository productRepository;

//     @Autowired
//     public ProductDAO(ProductRepository productRepository) {
//         this.productRepository = productRepository;
//     }

//     public Product save(Product product){
//         return productRepository.save(product);
//     }

//     public Product findById(Integer id) {
//         return productRepository.findById(id).orElse(null);
//     }

//     public Iterable<Product> findAll() {
//         return productRepository.findAll();
//     }

//     public void delete(Integer id) {
//         productRepository.deleteById(id);
//     }

    
// }


@Repository
public class ProductDAO {
    // @Autowired
    private final RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOps;

    @Autowired
    public ProductDAO(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    public Product save(Product product) {
        valueOps.set(String.valueOf(product.getProductId()), product);
        return product;
    }

    public Product findById(Integer id) {
        return (Product) valueOps.get(id.toString());
    }

    public Iterable<Product> findAll() {
        // Note: This method might be inefficient for larger datasets in Redis.
        return redisTemplate.keys("*").stream()
        .map(key -> (Product) valueOps.get(key))
        .collect(Collectors.toList());
    }

    public void delete(Integer id) {
        redisTemplate.delete(id.toString());
    }

    public boolean checkIfIdExist(String id) {
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.exists(id);
        }
    }
}