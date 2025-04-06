package com.study.mypizza.common.service;

import com.study.mypizza.common.dto.AuthorityDto;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.entity.Authority;
import com.study.mypizza.common.entity.Customer;
import com.study.mypizza.common.exception.MyPizzaException;
import com.study.mypizza.common.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {         // 회원 서비스
    private final CustomerRepository customerRepository;

}
