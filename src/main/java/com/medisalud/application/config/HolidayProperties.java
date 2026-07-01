package com.medisalud.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "medisalud.holidays")
public class HolidayProperties {
    
    private List<String> fixed = new ArrayList<>();
    private List<String> specific = new ArrayList<>();
    private Api api = new Api();
    
    @Data
    public static class Api {
        private boolean enabled = true;
        private String refreshCron = "0 0 0 1 1 *";
    }
}
