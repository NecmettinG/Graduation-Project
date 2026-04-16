package com.graduation.smarty_commerce.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

@Component
public class AppProperties {

    @Autowired
    private Environment env;

    public String getTokenSecret()
    {
        return env.getProperty("tokenSecret");
    }
}
