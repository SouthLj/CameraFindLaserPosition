package com.example.camerafindlaserposition;

public class Sc60Uart {

        /** NON_BLOCK */
         public static final int NON_BLOCK=0;
        /** BLOCK */
        public static final int BLOCK=1;

        private int fd=-1;
        private int blockModel=NON_BLOCK;

        public Sc60Uart(String uartName){
            open(uartName);
            setSerialPortParams(115200, 8,1,'n');
         }

        public Sc60Uart(String uartName, int baudrate,int dataBits,int stopBits,char parity){
            open(uartName);
            setSerialPortParams(baudrate, dataBits,stopBits,parity);
         }

        public Sc60Uart(String uartName, int baudrate){
            open(uartName);
            setSerialPortParams(baudrate, 8,1,'n');
         }

        public int setSerialPortParams(int baudrate,int dataBits,int stopBits,char parity){
            return setSerialPortParams_native(baudrate,dataBits,stopBits,parity,fd);
         }

        public String read_data(byte[] buf, int bufsize, int timeout){
            return read_data_native(buf,bufsize,timeout,fd);}

        public String read(byte[] buf, int bufsize){
            return read_native(buf,bufsize,fd);
        }

        public void close(){
        close_native(fd);
    }

        public int open(String uartName){
            fd=open_native(uartName);
            blockModel=NON_BLOCK;
            return fd>0?0:-1;
        }

        public int write(byte[] buf, int writesize){
            return write_native(buf,writesize,fd);
        }

        private native int setSerialPortParams_native(int baudrate,int dataBits,int stopBits,char parity,int fd);
        private native int open_native(String uartName);
        private native void close_native(int fd);
        private native String read_data_native(byte[] buf, int size, int timeout,int fd);
        private native String read_native(byte[] buf, int size,int fd);
        private native int write_native(byte[] buf, int writesize,int fd);

}
