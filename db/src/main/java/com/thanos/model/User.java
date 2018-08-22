package com.thanos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @Id
    private String uid;

    @Indexed
    //user id
    //personal prefers cell phone
    //org prefers email
    private String userId;

    @JsonIgnore
    private String password;

    // 0 - personal
    // 1 - org
    private int userType = 1; //by default


    private PersonalInfo personalInfo;

    private OrganizationInfo organizationInfo;

    static public class PersonalInfo {
        @TextIndexed String name;
        String idNum;  //身份证
        String fspoID; // the front side photo of ID
        String bspoID;  // the back side photo of ID
    }

    static public class OrganizationInfo {
        @TextIndexed String name; // 企业名称
        String linked; //联系人
        String linkedNum; //联系人手机号
        /**
         *  0 - 旅游行业
         *  1 - 互联网/游戏/软件
         *  2 - 电子/通信/硬件
         *  3 - 房地产/建筑/物业
         *  4 - 金融
         *  5 - 消费品
         *  6 - 汽车/机械/制造
         *  7 - 制造/医疗
         *  8 - 能源/化工/环保
         *  9 - 服务/外包/中介
         *  10 - 广告/传媒/教育/文化
         *  11 - 交通/贸易/物流
         *  12 - 政府/农林牧渔
         *  13 - 其他
         */
        @Indexed
        int type;


    }

    @Indexed
    private int verified = 0;


}
