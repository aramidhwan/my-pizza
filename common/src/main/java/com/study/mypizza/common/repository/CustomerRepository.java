package com.study.mypizza.common.repository;

import com.study.mypizza.common.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path="customers", collectionResourceRel = "customers")
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByCustomerId(String customerId);

    Optional<Customer> findOneByEmail(String email);

    @Query("SELECT c FROM Customer c JOIN FETCH c.authorities")
    List<Customer> findAllCustomers();

    boolean existsByEmail(String email);
}