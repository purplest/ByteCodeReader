package web.byteCode;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import web.annotation.Controller;
import web.annotation.Services;

/**
 * Created by xiang.xu on 2015/1/4.
 */
public class mixedTest {

    @Test
    public void test() throws IOException {
        InputStream is = mixedTest.class.getResourceAsStream("/web/controller/mixedController.class");
        InputStream is1 = mixedTest.class.getResourceAsStream("/web/controller/mixedController.class");
        ClassReader classReader = new ClassReader(is);
        ClassReader classReader1 = new ClassReader(is1);
        Assert.assertTrue(classReader.containsAnnotation(Controller.class));
        Assert.assertTrue(classReader1.containsAnnotation(Services.class));
    }
}
