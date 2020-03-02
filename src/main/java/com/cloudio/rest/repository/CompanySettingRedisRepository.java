package com.cloudio.rest.repository;

import com.cloudio.rest.pojo.CompanySettingRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompanySettingRedisRepository  {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, CompanySettingRedis> coffeeOps;


}
