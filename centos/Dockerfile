FROM centos:7.5.1804

ENV CMAKE_VERSION 3.17.0
ENV COLLECTD_VERSION 5.8.1
ENV GRPC_BR_VERSION v1.32.x
ENV PROTOBUF_VERSION 3.13.0

ENV MY_INSTALL_DIR /usr/local
ENV PATH="$PATH:$MY_INSTALL_DIR/bin"
RUN mkdir -p $MY_INSTALL_DIR

# NOTE: pkg-config default only check /usr/lib/pkgconfig and /usr/lib64/pkgconfig
ENV PKG_CONFIG_PATH $MY_INSTALL_DIR/lib/pkgconfig:$MY_INSTALL_DIR/lib64/pkgconfig
# NOTE: ldconfig default only check /usr/lib and /usr/lib64
ENV LD_LIBRARY_PATH $MY_INSTALL_DIR/lib:$MY_INSTALL_DIR/lib64

# Prerequisite libraries:
#     - curl wget git => basic packages for build
#     - bzip2 => required for collectd
#     - gcc gcc-c++ make => required for both grpc and collectd
#     - openssl openssl-devel => openssl package, required for both
#     - pkg-config => required for collectd
RUN yum update -y \
    && buildGrpcDeps=" \
        curl wget git bzip2 \
        gcc gcc-c++ make \
        openssl openssl-devel \
        pkg-config \
        " \
    && yum install -y $buildGrpcDeps \
    && g++ --version \
    && openssl version

# cmake installation
RUN wget -q -O cmake-linux.sh https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-Linux-x86_64.sh \
    && sh cmake-linux.sh -- --skip-license --prefix=$MY_INSTALL_DIR \
    && rm cmake-linux.sh \
    && cmake --version

######################################################
# Install grpc and protobuf for Collectd gRPC plugin #
######################################################
# reference: 
#     - https://grpc.io/docs/languages/cpp/quickstart    
#     - https://github.com/grpc/grpc/blob/master/BUILDING.md
#     - https://github.com/grpc/grpc/blob/master/test/distrib/cpp/run_distrib_test_cmake.sh
#     - https://github.com/grpc/grpc/blob/master/test/distrib/cpp/run_distrib_test_cmake_module_install.sh
# Clone the repository (including submodules)
RUN git clone --recurse-submodules -b ${GRPC_BR_VERSION} https://github.com/grpc/grpc
RUN cd grpc && mkdir -p cmake/build \
    && pushd cmake/build \
    && cmake -DgRPC_INSTALL=ON \
          -DgRPC_BUILD_TESTS=OFF \
          -DBUILD_SHARED_LIBS=ON \
          -DCMAKE_INSTALL_PREFIX=$MY_INSTALL_DIR \
          ../.. \
    && make -j$(nproc) \
    && make install \
    && popd \
    && pkg-config --version && pkg-config --list-all

#####################################
# Install Collectd with gRPC plugin #
#####################################
# reference:
#     - https://github.com/sandtable/collectd-grpc
RUN yum install -y bzip2 \
	&& curl -fSL "https://collectd.org/files/collectd-${COLLECTD_VERSION}.tar.bz2" -o "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && tar -xf "collectd-${COLLECTD_VERSION}.tar.bz2" \
    && rm "collectd-${COLLECTD_VERSION}.tar.bz2"
RUN cd /collectd-${COLLECTD_VERSION} \
    && ./configure \
         --prefix=/usr/local \
         --sysconfdir=/etc \
         --localstatedir=/var \
         --disable-dependency-tracking \
         --disable-static \
         --enable-grpc \
         PROTOBUF_CFLAGS="-std=c++11 $(pkg-config --cflags "protobuf")" \
        && make -j"$(nproc)" \
        && make install

# TODO: remove repositories, build packages to reduce the image size
#   - remove grpc and collectd-${COLLECTD_VERSION}
#   - remove centos updated list
#   - remove installed packages

CMD ["collectd", "-f"]
