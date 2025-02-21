package com.robin.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistryLite;
import io.grpc.MethodDescriptor;

import java.io.IOException;
import java.io.InputStream;

public class DynamicMessageMarshaller implements MethodDescriptor.Marshaller<DynamicMessage> {
    private final Descriptors.Descriptor descriptor;
    public DynamicMessageMarshaller(Descriptors.Descriptor descriptor){
        this.descriptor=descriptor;
    }

    @Override
    public InputStream stream(DynamicMessage dynamicMessage) {
        return dynamicMessage.toByteString().newInput();
    }

    @Override
    public DynamicMessage parse(InputStream inputStream) {
        try {
            return DynamicMessage.newBuilder(descriptor)
                    .mergeFrom(inputStream, ExtensionRegistryLite.getEmptyRegistry())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to merge from the supplied input stream", e);
        }
    }
}
