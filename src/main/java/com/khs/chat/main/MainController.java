package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
@RequestMapping(value = MainController.URL + "*", method = RequestMethod.GET)
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    protected static final String URL = "/";

    @Resource(name = "serverAsyncTaskService")
    private ServerAsyncTaskService serverAsyncTaskService;

    @Resource(name = "serverAsyncConfig")
    private ServerAsyncConfig serverAsyncConfig;
    private RandomChatRoom singleChatRoom;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(Model model) throws IOException {
        ModelAndView mav = new ModelAndView("main");
        singleChatRoom = RandomChatRoom.getInstance();
        logger.info("MainController()");
        // mav.addObject("ableToRunThread", serverAsyncConfig.checkSampleTaskExecute());
        mav.addObject("singleChatRooms", singleChatRoom.singleChatRooms.size());
        mav.addObject("currentSingleChatRoomUsers", singleChatRoom.currentSingleChatRoomUsers.size());
        return mav;
    }

}
