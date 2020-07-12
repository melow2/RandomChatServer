package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = MainController.URL+"*", method = RequestMethod.GET)
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    protected static final String URL = "/";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) throws Exception {
        // service.executeServer();
        logger.info("MainController()");
        return "main";

    }
}
