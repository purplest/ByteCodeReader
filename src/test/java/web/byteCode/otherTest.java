package web.byteCode;

import web.annotation.Controller;
import web.annotation.Services;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xiang.xu on 2015/1/4.
 */
public class otherTest {

    @Test
    public void test() throws IOException {
        InputStream is = indexTest.class.getResourceAsStream("/web/controller/otherController.class");
        ClassReader classReader = new ClassReader(is);
        Assert.assertTrue(classReader.containsAnnotation(Services.class));
    }

    @Test
    public void testa() throws IOException {
        InputStream is = indexTest.class.getResourceAsStream("/web/controller/otherController.class");
        ClassReader classReader = new ClassReader(is);
        Assert.assertFalse(classReader.containsAnnotation(Controller.class));

    }
}
