package com.leyou.client;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient("ly-item-service")
public interface CategoryClient extends CategoryApi {
}
