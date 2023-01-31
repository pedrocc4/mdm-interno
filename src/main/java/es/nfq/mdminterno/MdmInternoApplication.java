package es.nfq.mdminterno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MdmInternoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmInternoApplication.class, args);
    }

}
