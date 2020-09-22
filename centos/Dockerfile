FROM centos:7.5.1804

ENV CMAKE_VERSION 3.17.0
ENV COLLECTD_VERSION 5.8.1
ENV GRPC_BR_VERSION v1.32.x
ENV PROTOBUF_VERSION 3.13.0
ENV MY_INSTALL_DIR /usr/local
ENV PATH="$PATH:$MY_INSTALL_DIR/bin"

# NOTE: pkg-config default only check /usr/lib/pkgconfig and /usr/lib64/pkgconfig
ENV PKG_CONFIG_PATH $MY_INSTALL_DIR/lib/pkgconfig:$MY_INSTALL_DIR/lib64/pkgconfig

# NOTE: ldconfig default only check /usr/lib and /usr/lib64
ENV LD_LIBRARY_PATH $MY_INSTALL_DIR/lib:$MY_INSTALL_DIR/lib64

# Prerequisite libraries:
#   - curl, pkg-config packages are default installed in centos
#   - wget git => reqired for grpc build
#   - gcc gcc-c++ make openssl openssl-devel => required for grpc and collectd
#   - bzip2 => required for collectd

RUN mkdir -p $MY_INSTALL_DIR \
    # Install prerequisite libraries
    && yum update -y \
    && yum install -y wget git bzip2 gcc gcc-c++ make openssl openssl-devel \
    # cmake installation
    && wget -q -O cmake-linux.sh https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-Linux-x86_64.sh \
    && sh cmake-linux.sh -- --skip-license --prefix=$MY_INSTALL_DIR \
    && rm cmake-linux.sh \
    # Install grpc and protobuf for Collectd gRPC plugin
    # Clone the repository (including submodules)
    && git clone --recurse-submodules -b ${GRPC_BR_VERSION} https://github.com/grpc/grpc \
    && cd grpc && mkdir -p cmake/build \
    && pushd cmake/build \
    && cmake -DgRPC_INSTALL=ON \
          -DgRPC_BUILD_TESTS=OFF \
          -DBUILD_SHARED_LIBS=ON \
          -DCMAKE_INSTALL_PREFIX=$MY_INSTALL_DIR \
          ../.. \
    && make -j$(nproc) \
    && make install \
    && popd \
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
    && cd .. && rm -rf grpc collectd-${COLLECTD_VERSION} \
    && yum remove -y wget git bzip2 gcc gcc-c++ make openssl openssl-devel \
    && yum clean all \
    && rm -rf /var/cache/yum

CMD ["collectd", "-f"]
