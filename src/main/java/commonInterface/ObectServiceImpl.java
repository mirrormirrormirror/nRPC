package commonInterface;

import annotation.Impl;
import common.RequestsBody;

/**
 * Create by mirror on 2020/11/3
 */
@Impl(servicePath = "commonInterface.ObjectService")
public class ObectServiceImpl implements  ObjectService {
    @Override
    public RequestsBody getRequests(String mes) {
        RequestsBody requestsBody = new RequestsBody();
        requestsBody.setMethodName(mes);
        requestsBody.setMethodTypes("adfasdassad");
        return requestsBody;
    }
}
