package cn.touchair.audiobox.common;

import java.util.Arrays;

public class RawPacket {
    public byte[] header;
    public byte[] body;
    public byte[] tail;

    private RawPacket(byte[] header, byte[] body, byte[] tail) {
        this.header = header;
        this.body = body;
        this.tail = tail;
    }

    public void fillZero(int minSize) {
        if (header.length < minSize) {
            byte[] newHeader = new byte[minSize];
            Arrays.fill(newHeader, (byte) 0);
            System.arraycopy(header, 0, newHeader, 0, header.length);
            header = newHeader;
        }
        if (body.length < minSize) {
            byte[] newBody = new byte[minSize];
            Arrays.fill(newBody, (byte) 0);
            System.arraycopy(body, 0, newBody, 0, body.length);
            body = newBody;
        }
        if (tail.length < minSize) {
            byte[] newTail = new byte[minSize];
            Arrays.fill(newTail, (byte) 0);
            System.arraycopy(tail, 0, newTail, 0, tail.length);
            tail = newTail;
        }
    }

    public static class Builder {
        public byte[] header = new byte[0];
        public byte[] body = new byte[0];
        public byte[] tail = new byte[0];

        public Builder setHeader(byte[] header) {
            this.header = header;
            return this;
        }

        public Builder setHeader(short[] header) {
            return setHeader(BoxConvert.asByteArray(header));
        }

        public Builder setBody(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder setBody(short[] body) {
            return setBody(BoxConvert.asByteArray(body));
        }

        public Builder setTail(byte[] tail) {
            this.tail = tail;
            return this;
        }

        public Builder setTail(short[] tail) {
            return setTail(BoxConvert.asByteArray(tail));
        }

        public RawPacket build() {
            return new RawPacket(header, body, tail);
        }
    }
}