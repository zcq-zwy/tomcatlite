package com.nocoder.minitomcat.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author 29282
 */
@Slf4j
public class IOUtil {
    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

    public static byte[] getBytesFromFile(String fileName) throws IOException {
        InputStream in = IOUtil.class.getResourceAsStream(fileName);
        if (in == null) {
            logger.info("Not Found File:{}",fileName);
            throw new FileNotFoundException();
        }
        logger.info("正在读取文件:{}",fileName);
        return getBytesFromStream(in);
    }

    public static byte[] getBytesFromStream(InputStream in) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];     
        int len = -1;     
        while((len = in.read(buffer)) != -1){     
          outStream.write(buffer, 0, len);      
        }       
        outStream.close();      
        in.close();  
        return outStream.toByteArray();     
    }
   
}
