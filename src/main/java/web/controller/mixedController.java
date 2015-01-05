package web.controller;

import web.annotation.Controller;
import web.annotation.Services;

/**
 * Created by xiang.xu on 2015/1/4.
 */
@Controller(action = "t")
@Services(method = "asdf")
public class mixedController {
    public int asdf (){
        int a = 5;
        return a;
    }
}
