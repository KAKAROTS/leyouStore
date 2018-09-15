package com.leyou.auth.client;

import com.leyou.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-user-service")
public interface UserClinet extends UserApi {
}
