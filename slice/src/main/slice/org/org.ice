[["java:package:com.thanos.model"]]
module svc {

    struct RelatedPerson {
        string name;
        string title; //职位
    }

    struct Org {
        string name;
        string juridical; //法人
        string uscc; //unified social credit code 统一社会信用代码
        string tin; //Taxpayer identification number 纳税人识别号
        string registerNumber; // 注册号
        string code; //组织机构代码
        string category; //所属行业
        string ra; // registration authority 登记机关
        string region; //所属地区
        string nub; //曾用名 name used before
        string term; //business scope 公司类型
        string addr; //地址
        string doa; //date of apporval 核准日期
        string scope; // business scope 营业范围
        string status; //登记状态
        string founded; //成立时间

        string period; //经营期限

        string registeredCapital; //注册资本


    };
};