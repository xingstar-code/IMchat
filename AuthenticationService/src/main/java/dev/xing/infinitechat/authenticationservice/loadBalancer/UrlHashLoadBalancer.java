package dev.xing.infinitechat.authenticationservice.loadBalancer;



import org.springframework.cloud.client.ServiceInstance;

import java.util.HashMap;
import java.util.List;
@SuppressWarnings({"all"})
public class UrlHashLoadBalancer implements LoadBalancer {

    @Override
    public ServiceInstance select(List<ServiceInstance> instances, String userId) {
        ConsistentHash consistentHash = new ConsistentHash(instances);
        HashMap<String, ServiceInstance> map = consistentHash.map;
        return map.get(consistentHash.getServer(userId));
    }
}
