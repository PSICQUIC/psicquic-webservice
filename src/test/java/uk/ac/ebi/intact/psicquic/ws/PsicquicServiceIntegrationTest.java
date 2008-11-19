package uk.ac.ebi.intact.psicquic.ws;

import org.junit.Test;
import org.junit.Ignore;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.hupo.psi.mi.psicquic.DbRefRequest;
import org.hupo.psi.mi.psicquic.PsicquicService;
import psidev.psi.mi.xml254.jaxb.EntrySet;


public class PsicquicServiceIntegrationTest {

//	@Test
//	public void getVersion() throws Exception {
//		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
//		factory.setServiceClass(PsicquicService.class);
//		factory.setAddress("http://localhost:8080/psicquic-ws/webservices/psicquic");
//		PsicquicService client = (PsicquicService) factory.create();
//
//		String response = client.getVersion();
//		System.out.println("Received response from webservice: " + response);
//	}

    @Test @Ignore
	public void getVersion2() throws Exception {
		//lookup client
	    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"spring-test.xml"});

	    PsicquicService client = (PsicquicService)context.getBean("psicquicServiceClient");

        System.out.println("Version: " +client.getVersion());

        DbRefRequest request = new DbRefRequest();
        request.setBlockSize(50);
        request.setFirstResult(0);

        final EntrySet entrySet = client.getByInteractor(request).getResultSet().getEntrySet();
        System.out.println("Version: "+entrySet.getLevel()+"."+entrySet.getVersion()+"."+entrySet.getMinorVersion());
        System.out.println("Entries: "+entrySet.getEntries().size());
    }

}