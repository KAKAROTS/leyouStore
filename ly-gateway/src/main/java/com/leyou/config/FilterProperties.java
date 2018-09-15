package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
/**
 * @author xiongzixuan
 */
@Data
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {
    private List<String> allowPaths;
}
