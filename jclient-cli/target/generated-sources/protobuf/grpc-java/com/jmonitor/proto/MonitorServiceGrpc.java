package com.jmonitor.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.62.2)",
    comments = "Source: monitor.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MonitorServiceGrpc {

  private MonitorServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "jmonitor.MonitorService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.jmonitor.proto.MonitorStatsRequest,
      com.jmonitor.proto.MonitorStatsResponse> getGetStatsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStats",
      requestType = com.jmonitor.proto.MonitorStatsRequest.class,
      responseType = com.jmonitor.proto.MonitorStatsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.jmonitor.proto.MonitorStatsRequest,
      com.jmonitor.proto.MonitorStatsResponse> getGetStatsMethod() {
    io.grpc.MethodDescriptor<com.jmonitor.proto.MonitorStatsRequest, com.jmonitor.proto.MonitorStatsResponse> getGetStatsMethod;
    if ((getGetStatsMethod = MonitorServiceGrpc.getGetStatsMethod) == null) {
      synchronized (MonitorServiceGrpc.class) {
        if ((getGetStatsMethod = MonitorServiceGrpc.getGetStatsMethod) == null) {
          MonitorServiceGrpc.getGetStatsMethod = getGetStatsMethod =
              io.grpc.MethodDescriptor.<com.jmonitor.proto.MonitorStatsRequest, com.jmonitor.proto.MonitorStatsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStats"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.jmonitor.proto.MonitorStatsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.jmonitor.proto.MonitorStatsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MonitorServiceMethodDescriptorSupplier("GetStats"))
              .build();
        }
      }
    }
    return getGetStatsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MonitorServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MonitorServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MonitorServiceStub>() {
        @java.lang.Override
        public MonitorServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MonitorServiceStub(channel, callOptions);
        }
      };
    return MonitorServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MonitorServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MonitorServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MonitorServiceBlockingStub>() {
        @java.lang.Override
        public MonitorServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MonitorServiceBlockingStub(channel, callOptions);
        }
      };
    return MonitorServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MonitorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MonitorServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MonitorServiceFutureStub>() {
        @java.lang.Override
        public MonitorServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MonitorServiceFutureStub(channel, callOptions);
        }
      };
    return MonitorServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getStats(com.jmonitor.proto.MonitorStatsRequest request,
        io.grpc.stub.StreamObserver<com.jmonitor.proto.MonitorStatsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetStatsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MonitorService.
   */
  public static abstract class MonitorServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MonitorServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MonitorService.
   */
  public static final class MonitorServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MonitorServiceStub> {
    private MonitorServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MonitorServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MonitorServiceStub(channel, callOptions);
    }

    /**
     */
    public void getStats(com.jmonitor.proto.MonitorStatsRequest request,
        io.grpc.stub.StreamObserver<com.jmonitor.proto.MonitorStatsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetStatsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MonitorService.
   */
  public static final class MonitorServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MonitorServiceBlockingStub> {
    private MonitorServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MonitorServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MonitorServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.jmonitor.proto.MonitorStatsResponse getStats(com.jmonitor.proto.MonitorStatsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetStatsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MonitorService.
   */
  public static final class MonitorServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MonitorServiceFutureStub> {
    private MonitorServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MonitorServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MonitorServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.jmonitor.proto.MonitorStatsResponse> getStats(
        com.jmonitor.proto.MonitorStatsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetStatsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_STATS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_STATS:
          serviceImpl.getStats((com.jmonitor.proto.MonitorStatsRequest) request,
              (io.grpc.stub.StreamObserver<com.jmonitor.proto.MonitorStatsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetStatsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.jmonitor.proto.MonitorStatsRequest,
              com.jmonitor.proto.MonitorStatsResponse>(
                service, METHODID_GET_STATS)))
        .build();
  }

  private static abstract class MonitorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MonitorServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.jmonitor.proto.MonitorProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MonitorService");
    }
  }

  private static final class MonitorServiceFileDescriptorSupplier
      extends MonitorServiceBaseDescriptorSupplier {
    MonitorServiceFileDescriptorSupplier() {}
  }

  private static final class MonitorServiceMethodDescriptorSupplier
      extends MonitorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MonitorServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MonitorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MonitorServiceFileDescriptorSupplier())
              .addMethod(getGetStatsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
