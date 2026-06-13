package Cloudian.JobPortal.modules.test;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
@Tag(name = "Test", description = "Development test endpoints (not for production use)")
public class TestController {
    @GetMapping
    @Operation(summary = "Test server", description = "Simple test endpoint to verify the server is running. For development purposes.")
    public String testing()
    {
//      throw new UnauthorizedException("Help");
        return "Testing Successfully. This is the server, do you know it";
    }
}
