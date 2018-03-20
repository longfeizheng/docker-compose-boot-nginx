package cn.merryyou.docker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created on 2018/3/20 0020.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
@Controller
public class DockerController {

    @GetMapping("/")
    public String docker() {
        return "docker";
    }
}
