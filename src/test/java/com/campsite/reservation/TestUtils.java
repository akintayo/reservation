package com.campsite.reservation;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class TestUtils {

    /**
     * Starts an embedded version of Hazelcast so that tests can be run without running Hazelcast in a
     * docker container
     *
     */
    public static HazelcastInstance startHazelCastEmbedded() {

        Config config = new Config();
        config.setProperty("hazelcast.shutdownhook.enabled", "false");
        NetworkConfig network = config.getNetworkConfig();
        network.getJoin().getTcpIpConfig().setEnabled(false);
        network.getJoin().getMulticastConfig().setEnabled(false);
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        return instance;
    }
}
