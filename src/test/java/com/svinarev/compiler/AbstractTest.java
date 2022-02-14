package com.svinarev.compiler;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ActiveProfiles({"test"})
@SpringBootTest
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class AbstractTest {

}
