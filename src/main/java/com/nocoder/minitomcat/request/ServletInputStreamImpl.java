package com.nocoder.minitomcat.request;

import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * <p>作者： zcq</p>
 * <p>文件名称: ServletInputStreamImpl </p>
 * <p>描述: [类型描述] </p>
 * <p>创建时间: 2025/4/24 </p>
 *
 * @author <a href="mail to: 2928235428@qq.com" rel="nofollow">作者</a>
 * @version 1.0
 **/
public class ServletInputStreamImpl extends ServletInputStream {

  private final byte[] data;
  private int lastIndexRetrieved = -1;
  private ReadListener readListener = null;

  public ServletInputStreamImpl(byte[] data) {
    this.data = data;
  }

  @Override
  public boolean isFinished() {
    return lastIndexRetrieved == data.length - 1;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    this.readListener = readListener;
    if (!isFinished()) {
      try {
        readListener.onDataAvailable();
      } catch (IOException e) {
        readListener.onError(e);
      }
    } else {
      try {
        readListener.onAllDataRead();
      } catch (IOException e) {
        readListener.onError(e);
      }
    }
  }

  @Override
  public int read() throws IOException {
    if (lastIndexRetrieved < data.length) {
      lastIndexRetrieved++;
      int n = data[lastIndexRetrieved];
      if (readListener != null && isFinished()) {
        try {
          readListener.onAllDataRead();
        } catch (IOException ex) {
          readListener.onError(ex);
          throw ex;
        }
      }
      return n;
    }
    return -1;
  }

  @Override
  public int available() throws IOException {
    return data.length - lastIndexRetrieved - 1;
  }

  @Override
  public void close() throws IOException {
    lastIndexRetrieved = data.length - 1;
  }
}
