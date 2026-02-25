package Cloudian.JobPortal.modules.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {
    @GetMapping
    public String testing()
    {
        return "Testing successfully. 12Hi Hello world dot heere coms heheheheheheheh,. Do you know";
    }
}
