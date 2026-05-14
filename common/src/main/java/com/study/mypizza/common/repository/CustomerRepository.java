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

    // Customer 엔티티가 가지고 있는 연관관계 필드(authorities)를 조인(fetch join)해서 같이 가져옴
    // JOIN FETCH는 일반 JOIN과 달리:
    //      1. 연관된 엔티티(authorities)를 즉시 로딩(eager loading) 해서 한 번의 쿼리로 가져옴.
    //      2. N+1 문제를 방지할 수 있음.
    // 실제 SQL : select c1_0.customer_no,c1_0.activated,a1_0.customer_no,a1_1.authority_name,c1_0.customer_id,c1_0.customer_name,c1_0.email,c1_0.extra_roles,c1_0.password
    //           from t_customer c1_0
    //           join t_customer_authority a1_0
    //                      on c1_0.customer_no=a1_0.customer_no
    //           join t_authority a1_1
    //                      on a1_1.authority_name=a1_0.authority_name
    @Query("SELECT c FROM Customer c JOIN FETCH c.authorities")
    List<Customer> findAllCustomers();

    boolean existsByEmail(String email);
}