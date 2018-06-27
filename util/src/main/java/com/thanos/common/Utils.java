package com.thanos.common;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.Topic;

import java.nio.charset.StandardCharsets;
import java.util.*;

/****************************************************************************
 Copyright (c) 2017 Louis Y P Chen.
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
public class Utils {

    public final static String M = "/app";

    public final static String ENDPOINT_NAME = "thanos";

    public final static String BROADCAST_NAME = "/topic";

    public final static String PTP_NAME = "/queue";

    public final static String SESSIONID = ":sessionId";

    public static final Topic KEYEVENT_EXPIRED_TOPIC = new PatternTopic("__keyevent@*__:expired");

    public static final String QRCODE = "qrcode";

    public static final int GENERATE_ID_LENGTH = 128;

    public static final String QRCODE_EXPIRED = "10"; //seconds

    public static final String APP_ID = "appid";

    public static String rand(int len, boolean onlyNum){
        String val = "";
        Random random = new Random();
        for(int i = 0; i < len; i++) {
            String charOrNum = onlyNum ? "num" : random.nextInt(2) % 2 == 0 ? "char" : "num";
            if( "char".equalsIgnoreCase(charOrNum) ) {
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char)(random.nextInt(26) + temp);
            }else if("num".equalsIgnoreCase(charOrNum)){
                val += String.valueOf(random.nextInt(10));
            }
        }
        return  val;
    }

    public static String sign(String... values){
        return Hashing.sha256().hashString(sort(Arrays.asList(values)), StandardCharsets.UTF_8).toString();
    }

    private static String sort(List<String> list){
        //#Tip: Arrays.asList is read-only
        List<String> anotherList = new ArrayList<>(list);
        anotherList.removeAll(Collections.singleton(null));
        Collections.sort(anotherList);
        return StringUtils.join(anotherList, "");
    }
}
