package com.back.integracion;

import com.back.BackApplication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
class ThymeleafTest extends BaseIntegrationTest {

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTemplateOutput() {
        Context context = new Context();
        context.setVariable("nombre", "Juan");
        context.setVariable("enlace", "http://localhost:8080/reset-password?token=12345-67890");

        String output = templateEngine.process("emails/email-recuperacion", context);
        System.out.println("OUTPUT_START");
        System.out.println(output);
        System.out.println("OUTPUT_END");
        
        if (!output.contains("token=12345-67890")) {
            throw new RuntimeException("Token missing or escaped incorrectly");
        }
    }
}
