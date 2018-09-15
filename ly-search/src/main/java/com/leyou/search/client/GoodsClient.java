package com.leyou.search.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient("ly-item-service")
public interface GoodsClient extends GoodsApi{
}
