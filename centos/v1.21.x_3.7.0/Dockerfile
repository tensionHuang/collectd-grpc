FROM centos:7.5.1804

ENV COLLECTD_VERSION 5.8.1
ENV GRPC_BR_VERSION v1.21.x
ENV PROTOBUF_VERSION 3.7.0

# Prerequisite libraries:
#   - curl, pkg-config packages are default installed in centos
#   - wget git => reqired for grpc build
#   - gcc gcc-c++ make automake autoconf libtool => required for grpc and collectd
#   - bzip2 => required for collectd
ENV BUILD_PKGS="wget git bzip2 gcc gcc-c++ make automake autoconf libtool which zlib-devel"

ENV MY_INSTALL_DIR /usr/local
ENV PATH="$PATH:$MY_INSTALL_DIR/bin"

# NOTE: pkg-config default only check /usr/lib/pkgconfig and /usr/lib64/pkgconfig
ENV PKG_CONFIG_PATH $MY_INSTALL_DIR/lib/pkgconfig:$MY_INSTALL_DIR/lib64/pkgconfig

# NOTE: ldconfig default only check /usr/lib and /usr/lib64
ENV LD_LIBRARY_PATH $MY_INSTALL_DIR/lib:$MY_INSTALL_DIR/lib64

RUN mkdir -p $MY_INSTALL_DIR \
    # Install prerequisite libraries
    && yum update -y \
    && yum install -y ${BUILD_PKGS} \
    # Install protobuf
    && wget https://github.com/google/protobuf/releases/download/v${PROTOBUF_VERSION}/protobuf-cpp-${PROTOBUF_VERSION}.tar.gz \
    && rm protobuf-cpp-${PROTOBUF_VERSION}.tar.gz
    && tar xzf protobuf-cpp-${PROTOBUF_VERSION}.tar.gz \
    && cd protobuf-${PROTOBUF_VERSION} \
    && ./configure --disable-static \
    && make \
    && make install \
    && cd .. \
    # Install grpc and protobuf for Collectd gRPC plugin
    && git clone -b ${GRPC_BR_VERSION} https://github.com/grpc/grpc \
    && cd grpc \
    && git submodule update --init -- third_party/cares \
    && git submodule update --init -- third_rty/boringssl \
    && git submodule update --init -- third_party/zlib \
    && make -j$(nproc) \
    && make install \
    && ldconfig \
    && cd .. \
    # Install Collectd with gRPC plugin
    && curl -fSL "https://collectd.org/files/collectd-${COLLECTD_VERSION}.tar.bz2" -o "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && tar -xf "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && rm "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && cd /collectd-${COLLECTD_VERSION} \
    && ./configure \
         --prefix=/usr/local \
         --sysconfdir=/etc \
         --localstatedir=/var \
         --disable-dependency-tracking \
         --disable-static \
         --enable-grpc \
         PROTOBUF_CFLAGS="-std=c++11 $(pkg-config --cflags "protobuf")" \
    && make -j"$(nproc)" \
    && make install \
    && cd .. && rm -rf grpc collectd-${COLLECTD_VERSION} protobuf-${PROTOBUF_VERSION} \
    && yum remove -y ${BUILD_PKGS} \
    && yum clean all \
    && rm -rf /var/cache/yum

CMD ["collectd", "-f"]
