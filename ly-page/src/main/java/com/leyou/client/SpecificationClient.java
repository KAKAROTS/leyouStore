package com.leyou.client;

import com.leyou.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient("ly-item-service")
public interface SpecificationClient extends SpecificationApi{
}
