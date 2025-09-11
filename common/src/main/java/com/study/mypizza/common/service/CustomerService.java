package com.study.mypizza.common.service;

import com.study.mypizza.common.dto.AuthorityDto;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.entity.Customer;
import com.study.mypizza.common.exception.MyPizzaException;
import com.study.mypizza.common.repository.AuthorityRepository;
import com.study.mypizza.common.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
// readOnly = true는 JPA에서 dirty checking을 비활성화해서 불필요한 변경 감지를 하지 않습니다.
// 모든 Public 메소드에만 적용됨. 단, 내부 메소드끼리 호출에서는 적용안됨
@Transactional(readOnly = true)
public class CustomerService {         // 회원 서비스
    private final CustomerRepository customerRepository;
    private final AuthorityRepository authorityRepository ;

    public CustomerDto getCustomer(String email) {
        CustomerDto customerDto = customerRepository.findOneByEmail(email)
                .map(CustomerDto::of)
                .orElseThrow(RuntimeException::new)
                ;
        Hibernate.initialize(customerDto.getAuthorities());
        return customerDto ;
    }

    public List<AuthorityDto> getRoles() {
        return authorityRepository.findAll()
                .stream()
                .map(AuthorityDto::of)
                .toList();
    }

    // 회원 리스트
    public List<CustomerDto> getCustomers() throws MyPizzaException {
//        List<CustomerDto> customerDtos = customerRepository.findAll(Sort.by(Sort.Direction.ASC, "customerId"))
//                .stream()
//                .map(CustomerDto::of)
//                .toList()
//                ;
//        for (CustomerDto customerDto:customerDtos) {
//            Hibernate.initialize(customerDto.getAuthorities());
//        }

        return customerRepository.findAllCustomers()
                .stream()
                .map(CustomerDto::of)
                .toList();
    }

    @Transactional
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        Customer customer = customerRepository.findOneByEmail(customerDto.getEmail())
                .orElseThrow(RuntimeException::new);
        customer.setAuthorities(customerDto.getAuthorities().stream().map(AuthorityDto::toEntity).collect(Collectors.toList()));
        return CustomerDto.of(customerRepository.save(customer));
    }

}
