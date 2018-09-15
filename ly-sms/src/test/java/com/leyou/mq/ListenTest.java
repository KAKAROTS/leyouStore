package com.leyou.mq;

import com.leyou.config.Smsproperties;
import com.leyou.utils.SmsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class ListenTest {
    @Autowired
    private SmsUtil smsUtil;
    @Autowired
    private Smsproperties smsproperties;

    @Test
    public void sendMsg() {
        String phone="18616567781";
       Map<String, String> msg = new HashMap<>();
       msg.put("code","186735");
        smsUtil.sendSms(smsproperties.getSignName(),smsproperties.getVerifyCodeTemplate(),phone,msg);
    }
}