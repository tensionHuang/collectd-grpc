FROM debian:jessie
# This Dockerfile is based on a Dockerfile created by Angry Bytes
MAINTAINER Angry Bytes <info@angrybytes.com>

ENV COLLECTD_VERSION 5.7.1
ENV PROTOBUF_VERSION 3.2.0

# Install grpc and protobuf for Collectd gRPC plugin
RUN buildGrpcDeps=" \
		curl \
		ca-certificates \
		build-essential \
		git \
		autoconf \
		libtool \
		automake \
		wget \
    " \
	&& apt-get update \
	&& apt-get install -y --no-install-recommends $buildGrpcDeps \
	&& git clone -b $(curl -L http://grpc.io/release) https://github.com/grpc/grpc \
    && cd grpc \
    && git submodule update --init \
    && make \
    && make install \
    && cd .. \
    && wget https://github.com/google/protobuf/releases/download/v${PROTOBUF_VERSION}/protobuf-cpp-${PROTOBUF_VERSION}.tar.gz \
    && tar xzf protobuf-cpp-${PROTOBUF_VERSION}.tar.gz \
    && cd protobuf-${PROTOBUF_VERSION} \
    && ./configure \
    && make \
    && make install \
    && ldconfig \
    && cd .. \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false -o APT::AutoRemove::SuggestsImportant=false $buildGrpcDeps \
    && rm -fr protobuf-cpp-${PROTOBUF_VERSION}.tar.gz protobuf-${PROTOBUF_VERSION} grpc

# Install Collectd with gRPC plugin
RUN buildDeps=" \
        curl \
        ca-certificates \
        bzip2 \
        build-essential \
        bison \
        flex \
        autotools-dev \
        libltdl-dev \
        pkg-config \
        librrd-dev \
        linux-libc-dev \
    " \
    runDeps=" \
        libltdl7 \
        librrd4 \
    " \
    && set -x \
    && apt-get install -y --no-install-recommends $buildDeps $runDeps \
    && rm -rf /var/lib/apt/lists/* \
	&& curl -fSL "https://collectd.org/files/collectd-${COLLECTD_VERSION}.tar.bz2" -o "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && tar -xf "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && rm "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && ( \
        cd "collectd-${COLLECTD_VERSION}" \
        && ./configure \
            --prefix=/usr/local \
            --sysconfdir=/etc \
            --localstatedir=/var \
            --disable-dependency-tracking \
            --disable-static \
            --enable-grpc \
        && make -j"$(nproc)" \
        && make install \
    ) \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false -o APT::AutoRemove::SuggestsImportant=false $buildDeps \
    && rm -fr "collectd-${COLLECTD_VERSION}"

CMD ["collectd", "-f"]