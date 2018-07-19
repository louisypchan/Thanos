[["java:package:com.thanos.service"]]

#include "org.ice"

module org {

    interface OrgService {
        idempotent svc::Org getOrgInfo(string name);
    }
};