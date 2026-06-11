package dev.xing.infinitechat.realtimecommunicationservice.loadBalancer;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface LoadBalancer {

    ServiceInstance select(List< ServiceInstance> instances, String address);
}
