package com.leyou.test;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.config.Smsproperties;
import com.leyou.utils.SmsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsUtilTest {
    @Autowired
    private Smsproperties smsproperties;
    @Autowired
    private SmsUtil smsUtil;

    @Test
    public void sendSms() throws ClientException {
        Map<String, String> m = new HashMap<>();
        m.put("code","836126");
        //String serialize = JsonUtils.serialize(m);
        smsUtil.sendSms(smsproperties.getSignName(),smsproperties.getVerifyCodeTemplate(),"18616567781",m);
    }

}