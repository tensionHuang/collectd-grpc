### Run docker example: 

`docker run -p <GRPC_PORT>:<GRPC_PORT> -v /path/to/collectd.conf:/etc/collectd.conf:ro celine/collectd-grpc:latest`

Remember to edit `collectd.conf` with your own values

### Example in Python sending data with gRPC:

 - Install grpc (example: `pip install grpcio==1.1.3 grpcio-tools==1.1.3`)
 - Create stubs: `python -m grpc.tools.protoc -I/usr/local/include -I./proto --python_out=./ --grpc_python_out=./ ./collectd.proto` and `python -m grpc.tools.protoc -I/usr/local/include -I./proto --python_out=./ --grpc_python_out=./ ./types.proto`
 - Replace `host` and `GRPC_PORT` in `example_send_data.py` by the host and port where collectd-grpc is running
 - Run `example_send_data.py` (`python example_send_data.py`)
