### Notes
* install `collectd-utils`
    - use `collectdctl` to check collectd version
    - enable UDS plugin and then we can test
      ```
        LoadPlugin unixsock
        <Plugin unixsock>
          SocketFile "/tmp/collectd_uds_test"
          SocketGroup "root"
          SocketPerms "0770"
          DeleteSocket true
        </Plugin>
      ```
    - show the latest metrics `$collectdctl -s /tmp/collectd_uds_test listval`
    - put metric `$collectdctl -s -s /tmp/collectd_uds_test putval "testhost/plugin-pluginInstance/type-metricName" interval=10 1179574444:123:456`
    - get metric `$collectdctl -s -s /tmp/collectd_uds_test getval "testhost/plugin-pluginInstance/type-metricName"`

#### Collectd metric format in plain test

```

```

#### Collectd Installation
