import sys
import time

import grpc

import service_pb2
import types_pb2
import collectd_pb2
from google.protobuf import duration_pb2
from google.protobuf import timestamp_pb2

NB_ITERATION = 123
SLEEP = 10

def main(args):
	# the host where collectd-grpc is running
	#host = args[1] if args else 'localhost'
	host = "where.collectd-grpc.is.running"
	#host = "localhost"
	# Change port with the port collectd-grpc is running on
	GRPC_PORT = 1234
	
	channel = grpc.insecure_channel('{}:{}'.format(host, GRPC_PORT))

	stub = collectd_pb2.CollectdStub(channel)
	# stub.QueryValues(collectd_pb2.QueryValuesRequest())
	time1 = timestamp_pb2.Timestamp()
	time1.GetCurrentTime()
	time.sleep(SLEEP)
	# Dummy metric that just increments from 0 to NB_ITERATION
	for metric in range(NB_ITERATION):
		time2 = timestamp_pb2.Timestamp()
		time2.GetCurrentTime()
		duration = duration_pb2.Duration()
		duration.seconds = time2.seconds - time1.seconds
		duration.nanos = time2.nanos - time1.nanos
		data_metric = types_pb2.ValueList(
			values=[types_pb2.Value(gauge=metric)], 
			time=time2, 
			interval=duration, 
			identifier=types_pb2.Identifier(
				host='collectd', 
				plugin='grpc', 
				plugin_instance='grpc1', 
				type='gauge', 
				type_instance='metric_name'
			)
		)
		payload=[
			collectd_pb2.PutValuesRequest(value_list=data_metric)
		]
		time1 = timestamp_pb2.Timestamp()
		time1.GetCurrentTime()
		time.sleep(SLEEP)
    	
		def _get_iterator():
		    for data in payload:
		        yield data

		data_iterator = _get_iterator()

		stub.PutValues(data_iterator)

if __name__ == '__main__':
	main(sys.argv[1:])
