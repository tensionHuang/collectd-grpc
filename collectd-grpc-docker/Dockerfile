
#
FROM celine/collectd-grpc:latest

# Exec plugin can not run as root user
# Therefore, create new user 'collectd' in root group
RUN useradd -G root collectd
RUN mkdir /scripts
COPY simple_counter.sh /scripts


RUN mkdir /usr/share/collectd
COPY types.db /usr/share/collectd/types.db