/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafka.streams.state.utils.tests;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
/**
 *
 * @author Jouni Ahto
 */
@RunWith(SpringRunner.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = { "messages"})

public class SimpleTests {
    @Autowired
    private KafkaEmbedded embeddedKafka;
    
    @Test
    public void placeHolder() {
        // Temporary placeholder while the tests are still to be written.
        // In the meanwhile, keeps those tools that insist on running tests
        // on every build happy and not error on missing tests (at least NetBeans).
    }
}
