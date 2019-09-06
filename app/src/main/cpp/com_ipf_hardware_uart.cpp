//
// Created by liujian on 2019/7/2.
//
#include "jni.h"
#include <fcntl.h>
#include <stdio.h>
#include <termios.h>
#include "ql_log.h"
#include<unistd.h>
#include <string.h>
//#include <>

/**
 * @author liujian
 * @date 2019-07-02
 * @brief open uart
 * @param deviceName:uart index
 * @return file descriptor
 */
static int uartOpen(char const* deviceName){
    LOGE("uartOpen()-->:deviceName = %s",deviceName);
    int fd=open(deviceName,O_RDWR);//读写方式 int fd=open(deviceName,O_RDWR|O_NONBLOCK);
    if(fd<0){
        LOGE("uartOpen()-->:fd open failure");
        return -1;
    }

    LOGW("uartOpen()-->: open device success");
    return fd;

}
/**
 * @author liujian
 * @date 2019-07-02
 * @brief close uart
 * @param fd:file descriptor
 * @return NULL
 */
static void uartClose(int fd){
    close(fd);
}
/**
 * @author liujian
 * @date 2019-07-02
 * @brief read select
 * @param fd:file descriptor timeout:
 * @return if 1 data comes
 */
static int uart_readable(int timeout,int fd)
{
    int ret;
    fd_set set;
    struct timeval tv = { timeout / 1000, (timeout % 1000) * 1000 } ;

    FD_ZERO (&set);
    FD_SET (fd, &set);

    ret = select (fd + 1, &set, NULL, NULL, &tv);

    if (ret > 0){
        return 1;
    }

    return 0;
}
/**
 * @author liujian
 * @date 2019-07-02
 * @brief read data
 * @param buf:data buffer size:buffer size timeout:time fd :file descriptor
 * @return file descriptor
 */
static int uart_read(char* buf, int size, int timeout,int fd)
{
    //LOGI("uart_read select model......");

    int got = 0, ret;
    do {
        ret = read (fd, buf + got, size - got);
       // LOGI("ret %d.....",ret);
       // LOGI("buff1 %s....",buf);
        if (ret > 0 ) got += ret;
        if (got >= size) break;
    }
    while (uart_readable(timeout,fd));

   // LOGI("got %d.....",got);
    return got;
}

/**
 * @author liujian
 * @date 2019-07-02
 * @brief read data
 * @param buf:data buffer size:buffer size timeout:time fd :file descriptor
 * @return file descriptor
 */
static int uart_read_nolock(char* buf, int size,int fd)
{
    //LOGI("uart_read select model......");

    int  ret = -1;

    ret = read (fd, buf , size);


    // LOGI("got %d.....",got);
    return ret;
}
/**
 * @author liujian
 * @date 2019-07-02
 * @brief write data
 * @param buf:data buffer size:buffer size timeout:time fd :file descriptor
 * @return file descriptor
 */
int uartWrite(char *send_buf,int data_len,int fd){
    int len = 0;
    LOGW("send_buf: %s",send_buf);
    len = write(fd,send_buf,data_len);
    if (len!=data_len ){
        LOGW("<<<>>>>has send data %d, but not equal %d",len,data_len);
    }else{
        LOGW("send data to uart: %d, fd is %d",len,fd);
    }
    return len;
}
/**
 * @author liujian
 * @date 2019-07-02
 * @brief set serial
 * @param speed: databits: stopbits: parity: fd:
 * @return success if return 0
 */
int uartSetSerial(int speed,int databits,int stopbits, char parity,int fd){
    int i;
    int status;
    int speed_arr[] = {B115200, B38400, B19200, B9600, B4800, B2400, B1200, B300,
                       B38400, B19200, B9600, B4800, B2400, B1200, B300 };
    int name_arr[] = {115200,38400,  19200,  9600,  4800,  2400,  1200,  300,      38400, 19200,  9600, 4800, 2400, 1200,  300 };

    struct termios options;

    if(tcgetattr( fd,&options)!=0){
        perror("SetupSerial 1");
        return -1;
    }

    //设置串口输入波特率和输出波特率
    for ( i= 0; i<sizeof(speed_arr)/sizeof(int); i++){
        if  (speed == name_arr[i]){
            cfsetispeed(&options, speed_arr[i]);
            cfsetospeed(&options, speed_arr[i]);
        }
    }

    //修改控制模式，保证程序不会占用串口
    options.c_cflag |= CLOCAL;

    //修改控制模式，使得能够从串口中读取输入数据
    options.c_cflag |= CREAD;

    //设置数据位
    options.c_cflag &= ~CSIZE; //屏蔽其他标志位
    switch (databits){
        case 5    :
            options.c_cflag |= CS5;
            break;
        case 6    :
            options.c_cflag |= CS6;
            break;
        case 7    :
            options.c_cflag |= CS7;
            break;
        case 8:
            options.c_cflag |= CS8;
            break;
        default:
            fprintf(stderr,"Unsupported data size/n");
            return (-1);
    }

    //设置校验位
    switch (parity){
        case 'n':
        case 'N': //无奇偶校验位。
            options.c_cflag &= ~PARENB;
            options.c_iflag &= ~INPCK;
            break;
        case 'o':
        case 'O'://设置为奇校验
            options.c_cflag |= (PARODD | PARENB);
            options.c_iflag |= INPCK;
            break;
        case 'e':
        case 'E'://设置为偶校验
            options.c_cflag |= PARENB;
            options.c_cflag &= ~PARODD;
            options.c_iflag |= INPCK;
            break;
        case 's':
        case 'S': //设置为空格
            options.c_cflag &= ~PARENB;
            options.c_cflag &= ~CSTOPB;
            break;
        default:
            fprintf(stderr,"Unsupported parity/n");
            return (-1);
    }

    // 设置停止位
    switch (stopbits){
        case 1:
            options.c_cflag &= ~CSTOPB;
            break;
        case 2:
            options.c_cflag |= CSTOPB;
            break;
        default:
            fprintf(stderr,"Unsupported stop bits/n");
            return (-1);
    }

    //修改输出模式，原始数据输出
    options.c_lflag  &= ~(ICANON | ECHO | ECHOE | ISIG);  /*Input*/
    options.c_oflag &= ~OPOST;

    //设置等待时间和最小接收字符

    options.c_cc[VTIME] = 1; /* 读取一个字符等待1*(1/10)s */

    options.c_cc[VMIN] = 1; /* 读取字符的最少个数为1 */

    options.c_iflag &=~(ICRNL | IGNCR );

    //如果发生数据溢出，接收数据，但是不再读取

    tcflush(fd,TCIFLUSH);

    //激活配置 (将修改后的termios数据设置到串口中）

    if (tcsetattr(fd,TCSANOW,&options) != 0)

    {

        perror("com set error!/n");

        return (-1);

    }

    return (0);

}
/*
static JNINativeMethod method_table[] = {
        { "close_native", "(I)V", (void*)close },
        { "open_native", "(Ljava/lang/String;)I", (void*)open2 },
        { "setBlock_native", "(II)I", (void*)setBlock },
        { "setSerialPortParams_native", "(IIIII)I", (void*)setSerialPortParams },
        { "setFlowControlMode_native", "(II)I", (void*)setFlowControlMode },
        { "select_native", "(II)I", (void*)uart_select},
        { "read_native", "([BIII)I", (void*)read2 },
        { "write_native", "([BII)I", (void*)write },
};
*/

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_camerafindlaserposition_Sc60Uart_open_1native(JNIEnv *env, jobject instance,
                                                      jstring uartName_) {
    const char *uartName = env->GetStringUTFChars(uartName_, 0);
    char item_value[128];
    strcpy(item_value, uartName);
    // TODO
    env->ReleaseStringUTFChars(uartName_, uartName);
    LOGD("item_value = %s", item_value);
    return uartOpen(item_value);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_camerafindlaserposition_Sc60Uart_close_1native(JNIEnv *env, jobject instance, jint fd) {

    uartClose(fd);
    // TODO

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_camerafindlaserposition_Sc60Uart_read_1data_1native(JNIEnv *env, jobject instance,
                                                            jbyteArray buf_, jint size,
                                                            jint timeout, jint fd) {

    // TODO
    char* data = (char*)env->GetByteArrayElements(buf_, NULL);
    int num = uart_read(data, size, timeout, fd);
    jstring mStr = env->NewStringUTF(data);
    return mStr;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_camerafindlaserposition_Sc60Uart_write_1native(JNIEnv *env, jobject instance,
                                                       jbyteArray buf_, jint writesize, jint fd) {
    jbyte *buf = env->GetByteArrayElements(buf_, NULL);

    // TODO

    //jbyte* arrayData = (jbyte*)env->GetByteArrayElements(buf_,0);
    return uartWrite((char *) buf, writesize, fd);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_camerafindlaserposition_Sc60Uart_setSerialPortParams_1native(JNIEnv *env, jobject instance,
                                                                     jint baudrate, jint dataBits,
                                                                     jint stopBits, jchar parity,
                                                                     jint fd) {

    // TODO
    jint mint = uartSetSerial(baudrate,dataBits,stopBits,parity,fd);
    LOGD("mint = %d", mint);
    return mint;

}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_camerafindlaserposition_Sc60Uart_read_1native(JNIEnv *env, jobject instance,
                                                               jbyteArray buf_, jint size,
                                                               jint fd) {
    char* data = (char*)env->GetByteArrayElements(buf_, NULL);
    int num = uart_read_nolock(data, size, fd);
    jstring mStr = env->NewStringUTF(data);
    return mStr;
}