FROM centos/collectd-grpc:latest

# Exec plugin can not run as root user
# Therefore, create new user 'collectd' in root group
RUN useradd -G root collectd
RUN mkdir /scripts
COPY scripts/simple_counter.sh /scripts
COPY conf/collectd.conf /etc


RUN mkdir /usr/share/collectd
COPY conf/types.db /usr/share/collectd/types.db