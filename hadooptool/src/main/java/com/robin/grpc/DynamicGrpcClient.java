package com.robin.grpc;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.Channel;
import com.google.protobuf.Descriptors.MethodDescriptor;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.grpc.MethodDescriptor.MethodType;

import java.util.List;

public class DynamicGrpcClient {
    private static final Logger logger= LoggerFactory.getLogger(DynamicGrpcClient.class);
    private final MethodDescriptor descriptor;
    private final Channel channel;
    DynamicGrpcClient(MethodDescriptor descriptor,Channel channel){
        this.descriptor=descriptor;
        this.channel=channel;
    }
    public static DynamicGrpcClient create(MethodDescriptor descriptor,Channel channel){
        return new DynamicGrpcClient(descriptor,channel);
    }
    public ListenableFuture<Void>  call(List<DynamicMessage> requests, StreamObserver<DynamicMessage> observer, CallOptions options){
        MethodType methodType = getMethodType();
        int numRequests = requests.size();
        if (methodType == MethodType.UNARY) {
            logger.info("Making unary call");
            Preconditions.checkArgument(numRequests == 1,
                    "Need exactly 1 request for unary call, but got: " + numRequests);
            return callUnary(requests.get(0), observer, options);
        } else if (methodType == MethodType.SERVER_STREAMING) {
            logger.info("Making server streaming call");
            Preconditions.checkArgument(numRequests == 1,
                    "Need exactly 1 request for server streaming call, but got: " + numRequests);
            return callServerStreaming(requests.get(0), observer, options);
        } else if (methodType == MethodType.CLIENT_STREAMING) {
            logger.info("Making client streaming call with " + requests.size() + " requests");
            return callClientStreaming(requests, observer, options);
        } else {
            // Bidi streaming.
            logger.info("Making bidi streaming call with " + requests.size() + " requests");
            return callBidiStreaming(requests, observer, options);
        }
    }
    private ListenableFuture<Void> callBidiStreaming(
            List<DynamicMessage> requests,
            StreamObserver<DynamicMessage> responseObserver,
            CallOptions callOptions) {
        DoneObserver<DynamicMessage> doneObserver=new DoneObserver<>();
        StreamObserver<DynamicMessage> requestObserver = ClientCalls.asyncBidiStreamingCall(
                createCall(callOptions),
                CompositeStreamObserver.of(responseObserver,doneObserver));
        requests.forEach(requestObserver::onNext);
        requestObserver.onCompleted();
        return doneObserver.getCompletionFuture();
    }

    private ListenableFuture<Void> callClientStreaming(
            List<DynamicMessage> requests,
            StreamObserver<DynamicMessage> responseObserver,
            CallOptions callOptions) {
        DoneObserver<DynamicMessage> doneObserver=new DoneObserver<>();
        StreamObserver<DynamicMessage> requestObserver = ClientCalls.asyncClientStreamingCall(
                createCall(callOptions),
                CompositeStreamObserver.of(responseObserver,doneObserver));
        requests.forEach(requestObserver::onNext);
        requestObserver.onCompleted();
        return doneObserver.getCompletionFuture();
    }

    private ListenableFuture<Void> callServerStreaming(
            DynamicMessage request,
            StreamObserver<DynamicMessage> responseObserver,
            CallOptions callOptions) {
        DoneObserver<DynamicMessage> doneObserver=new DoneObserver<>();
        ClientCalls.asyncServerStreamingCall(
                createCall(callOptions),
                request,
                CompositeStreamObserver.of(responseObserver,doneObserver));
        return doneObserver.getCompletionFuture();
    }

    private ListenableFuture<Void> callUnary(
            DynamicMessage request,
            StreamObserver<DynamicMessage> responseObserver,
            CallOptions callOptions) {
        DoneObserver<DynamicMessage> doneObserver=new DoneObserver<>();
        ClientCalls.asyncUnaryCall(
                createCall(callOptions),
                request,
                CompositeStreamObserver.of(responseObserver,doneObserver));
        return doneObserver.getCompletionFuture();
    }

    private ClientCall<DynamicMessage, DynamicMessage> createCall(CallOptions callOptions) {
        return channel.newCall(createGrpcMethodDescriptor(), callOptions);
    }

    private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor() {
        return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>create(
                getMethodType(),
                getFullMethodName(),
                new DynamicMessageMarshaller(descriptor.getInputType()),
                new DynamicMessageMarshaller(descriptor.getOutputType()));
    }

    private String getFullMethodName() {
        String serviceName = descriptor.getService().getFullName();
        String methodName = descriptor.getName();
        return io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName);
    }
    private MethodType getMethodType() {
        boolean clientStreaming = descriptor.toProto().getClientStreaming();
        boolean serverStreaming = descriptor.toProto().getServerStreaming();

        if (!clientStreaming && !serverStreaming) {
            return MethodType.UNARY;
        } else if (!clientStreaming && serverStreaming) {
            return MethodType.SERVER_STREAMING;
        } else if (clientStreaming && !serverStreaming) {
            return MethodType.CLIENT_STREAMING;
        } else {
            return MethodType.BIDI_STREAMING;
        }
    }

}
