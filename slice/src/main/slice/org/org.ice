[["java:package:com.thanos.model"]]
module svc {

    struct RelatedPerson {
        string name;
        string title; //职位
    }

    struct Partner {
        string name;
        string rate; //出资比例
        string scc;  //subscribed capital contribution 认缴出资
    }

    struct ChangeRecord {
        string time; //变更时间
        string category; //变更项目
        string before; //变更前
        string after; //变更后
    }

    struct Patent {
        string publishDate; //申请公布日
        string name; //专利名称
        string applyNo; //申请号
        string publishNo; //申请公布号
        string category; //专利类型
    }

    struct Brand {
        string applyDate; //申请日期
        string brand; //商标
        string name; //商标名称
        string registeredNo; //注册号
        string category; //类别
        string status; //流程状态
    }

    struct CopyRight{
        string approvalDate; //批准日期
        string name; //软件全称
        string cn; //软件简称
        string registeredNo; //登记号
        string category; //分类号
        string version; //版本号
    }

    sequence<svc::RelatedPerson> RelatedPersonSeq;
    sequence<svc::Partner> PartnerSeq;
    sequence<svc::Brand> BrandSeq;
    sequence<svc::CopyRight> CopyRightSeq;
    sequence<string> stringReq;

    struct Properties {
        string pId;
        PartnerSeq patents;
        BrandSeq brands;
        CopyRightSeq copyRights;
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

        RelatedPersonSeq relatedPersons;

        PartnerSeq partners;

        stringReq changeList; //变更记录

        stringReq investment; //对外投资

        stringReq subCompanies; //分支机构

        stringReq properties; //知识产权
    };
};