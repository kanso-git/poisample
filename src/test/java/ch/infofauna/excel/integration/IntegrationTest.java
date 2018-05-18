package ch.infofauna.excel.integration;



import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author: Kanso
 */
@ContextConfiguration(locations = {"classpath:META-INF/wfws-app-context.xml", "classpath:test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class IntegrationTest{



}
