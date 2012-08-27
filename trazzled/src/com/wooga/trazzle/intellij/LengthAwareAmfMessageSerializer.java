package com.wooga.trazzle.intellij;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf0Output;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LengthAwareAmfMessageSerializer extends AmfMessageSerializer {

    public void writeHeader(MessageHeader h) throws IOException
    {
        amfOut.writeUTF(h.getName());
        amfOut.writeBoolean(h.getMustUnderstand());
        Object data = h.getData();

        SerializationContext context = new SerializationContext();
        Amf0Output amfOutBuffer = new Amf0Output(context);
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        amfOutBuffer.setOutputStream(new BufferedOutputStream(tempOut));
        amfOutBuffer.setAvmPlus(true);
        amfOutBuffer.writeObject(data);
        amfOutBuffer.flush();

        amfOut.writeInt(tempOut.size());
        amfOut.reset();
        amfOut.writeObject(h.getData());
    }

    public void writeBody(MessageBody b) throws IOException
    {
        if (b.getTargetURI() == null)
            amfOut.writeUTF("null");
        else
            amfOut.writeUTF(b.getTargetURI());

        if (b.getResponseURI() == null)
            amfOut.writeUTF("null");
        else
            amfOut.writeUTF(b.getResponseURI());

        Object data = b.getData();

        SerializationContext context = new SerializationContext();
        Amf0Output amfOutBuffer = new Amf0Output(context);
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        amfOutBuffer.setOutputStream(new BufferedOutputStream(tempOut));
        amfOutBuffer.setAvmPlus(true);
        amfOutBuffer.writeObject(data);
        amfOutBuffer.flush();

        amfOut.writeInt(tempOut.size());
        amfOut.reset();

        amfOut.writeObject(data);
    }
}
