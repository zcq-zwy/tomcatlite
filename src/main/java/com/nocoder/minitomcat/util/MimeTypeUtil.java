package com.nocoder.minitomcat.util;

import eu.medsea.mimeutil.MimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

import static com.nocoder.minitomcat.constant.ContextConstant.DEFAULT_CONTENT_TYPE;

@Slf4j
public class MimeTypeUtil {
    static {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }

    public static String getTypes(String fileName) {
        if(fileName.endsWith(".html")){
            return DEFAULT_CONTENT_TYPE;
        }
        Collection mimeTypes = MimeUtil.getMimeTypes(MimeTypeUtil.class.getResource(fileName));
        return mimeTypes.toArray()[0].toString();
    }

}
