//https://dimitri.codes/spring-boot-mailpit/
package Cloudian.JobPortal.modules.email;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService {
    //Duong link token
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${app.mail.from}")
    String fromHost;
    public void sendEmail(String to , String subject , String template , Map<String , Object> variables) throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);

        var content =
                templateEngine.process(
                        template,
                        context
                );

        var mineMessage = javaMailSender.createMimeMessage();
        var message = new MimeMessageHelper(mineMessage , true , "UTF-8");
        //Send email
        message.setFrom(fromHost);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content , true);
        javaMailSender.send(message.getMimeMessage());
    }
}
