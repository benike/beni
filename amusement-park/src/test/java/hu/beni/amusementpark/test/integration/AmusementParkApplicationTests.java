package hu.beni.amusementpark.test.integration;

import static hu.beni.amusementpark.constants.ErrorMessageConstants.MACHINE_IS_TOO_EXPENSIVE;
import static hu.beni.amusementpark.constants.ErrorMessageConstants.validationError;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.ADD_REGISTRY;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.AMUSEMENT_PARK;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.GET_OFF_MACHINE;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.GET_ON_MACHINE;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.LOGIN;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.LOGOUT;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.MACHINE;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.ME;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.SIGN_UP;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.UPLOAD_MONEY;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.VISITOR_ENTER_PARK;
import static hu.beni.amusementpark.constants.HATEOASLinkRelConstants.VISITOR_LEAVE_PARK;
import static hu.beni.amusementpark.constants.StringParamConstants.OPINION_ON_THE_PARK;
import static hu.beni.amusementpark.constants.ValidationMessageConstants.oneOfMessage;
import static hu.beni.amusementpark.constants.ValidationMessageConstants.rangeMessage;
import static hu.beni.amusementpark.helper.MyAssert.assertThrows;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.TypeReferences.PagedResourcesType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import hu.beni.amusementpark.AmusementParkApplication;
import hu.beni.amusementpark.config.RestTemplateConfig;
import hu.beni.amusementpark.dto.resource.AmusementParkResource;
import hu.beni.amusementpark.dto.resource.GuestBookRegistryResource;
import hu.beni.amusementpark.dto.resource.MachineResource;
import hu.beni.amusementpark.dto.resource.VisitorResource;
import hu.beni.amusementpark.enums.MachineType;
import hu.beni.amusementpark.exception.AmusementParkException;
import hu.beni.amusementpark.helper.MyAssert.ExceptionAsserter;
import hu.beni.amusementpark.helper.ValidResourceFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { AmusementParkApplication.class,
		RestTemplateConfig.class })
public class AmusementParkApplicationTests {

	public static final PagedResourcesType<AmusementParkResource> PAGED_AMUSEMENT_PARK = new PagedResourcesType<AmusementParkResource>() {
	};

	private static Map<String, String> links;

	@Autowired
	private RestTemplate restTemplate;

	@LocalServerPort
	private int port;

	@PostConstruct
	public void init() {
		links = links == null ? getBaseLinks() : links;
	}

	private Map<String, String> getBaseLinks() {
		return Stream.of(restTemplate.getForObject("http://localhost:" + port + "/links", Link[].class))
				.collect(toMap(Link::getRel, Link::getHref));
	}

	@Test
	public void signUpAndUploadMoneyAndVisitorCanNotCreateParkTest() {
		VisitorResource inputVisitorResource = ValidResourceFactory.createVisitor();

		VisitorResource responseVisitorResource = signUp(inputVisitorResource);

		assertSignedUpVisitor(inputVisitorResource, responseVisitorResource, 250);

		uploadMoney500(responseVisitorResource.getLink(UPLOAD_MONEY).getHref());

		assertSignedUpVisitor(inputVisitorResource,
				restTemplate.getForObject(responseVisitorResource.getId().getHref(), VisitorResource.class), 750);

		getAmusementParksWorks();

		postAmusementParksAccessIsDenied();

		logout();
	}

	private VisitorResource signUp(VisitorResource visitorResource) {
		return restTemplate.postForObject(links.get(SIGN_UP), visitorResource, VisitorResource.class);
	}

	private void assertSignedUpVisitor(VisitorResource inputVisitorResource, VisitorResource actualVisitorResource,
			Integer spendingMoney) {
		assertEquals(inputVisitorResource.getEmail(), actualVisitorResource.getEmail());
		assertEquals(inputVisitorResource.getDateOfBirth(), actualVisitorResource.getDateOfBirth());
		assertEquals(spendingMoney.intValue(), actualVisitorResource.getSpendingMoney().intValue());
		assertEquals("ROLE_VISITOR", actualVisitorResource.getAuthority());
		assertNull(actualVisitorResource.getPassword());
		assertNull(actualVisitorResource.getConfirmPassword());
		assertEquals(3, actualVisitorResource.getLinks().size());
		assertTrue(actualVisitorResource.getId().getHref().endsWith("/me"));
		assertNotNull(actualVisitorResource.getLink(UPLOAD_MONEY).getHref());
		assertNotNull(actualVisitorResource.getLink(AMUSEMENT_PARK).getHref());
	}

	private void uploadMoney500(String uploadMoneyHref) {
		restTemplate.postForObject(uploadMoneyHref, 500, Void.class);
	}

	private void getAmusementParksWorks() {
		ResponseEntity<PagedResources<AmusementParkResource>> response = getAmusementParks();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(3, response.getBody().getContent().size());
	}

	private ResponseEntity<PagedResources<AmusementParkResource>> getAmusementParks() {
		return restTemplate.exchange(links.get(AMUSEMENT_PARK), HttpMethod.GET, HttpEntity.EMPTY, PAGED_AMUSEMENT_PARK);
	}

	private void postAmusementParksAccessIsDenied() {
		assertThrows(() -> restTemplate.postForEntity(links.get(AMUSEMENT_PARK),
				ValidResourceFactory.createAmusementPark(), Void.class), HttpClientErrorException.class, exception -> {
					assertEquals(HttpStatus.I_AM_A_TEAPOT, exception.getStatusCode());
					assertEquals("Access is denied", exception.getResponseBodyAsString());
				});
	}

	private void logout() {
		ResponseEntity<Void> response = restTemplate.postForEntity(links.get(LOGOUT), null, Void.class);

		assertEquals(HttpStatus.FOUND, response.getStatusCode());
		assertTrue(response.getHeaders().getLocation().toString().endsWith(Integer.toString(port) + "/"));
	}

	@Test
	public void pageTest() {
		loginAsAdmin();

		ResponseEntity<PagedResources<AmusementParkResource>> response = getAmusementParks();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		PagedResources<AmusementParkResource> page = response.getBody();
		assertEquals(1, page.getLinks().size());
		assertNotNull(page.getId());

		IntStream.range(0, 11).forEach(i -> postAmusementPark());

		response = getAmusementParks();
		assertEquals(HttpStatus.OK, response.getStatusCode());

		page = response.getBody();
		assertEquals(4, page.getLinks().size());
		assertNotNull(page.getLink("first"));
		assertNotNull(page.getLink("next"));
		assertNotNull(page.getLink("last"));

		response = restTemplate.exchange(page.getLink("last").getHref(), HttpMethod.GET, HttpEntity.EMPTY,
				PAGED_AMUSEMENT_PARK);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		page = response.getBody();
		assertEquals(4, page.getLinks().size());
		assertNotNull(page.getLink("first"));
		assertNotNull(page.getLink("prev"));
		assertNotNull(page.getLink("last"));

		response = restTemplate.exchange(links.get(AMUSEMENT_PARK) + "?input=" + encode("{\"name\":\"a\"}"),
				HttpMethod.GET, HttpEntity.EMPTY, PAGED_AMUSEMENT_PARK);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		page = response.getBody();
		assertEquals(4, page.getLinks().size());
		assertNotNull(page.getLink("last"));

		response = restTemplate.exchange(links.get(AMUSEMENT_PARK) + "?input=" + encode("{\"name\":\"x\"}"),
				HttpMethod.GET, HttpEntity.EMPTY, PAGED_AMUSEMENT_PARK);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		page = response.getBody();
		assertEquals(1, page.getLinks().size());
	}

	private VisitorResource loginAsAdmin() {
		ResponseEntity<VisitorResource> response = restTemplate.postForEntity(links.get(LOGIN),
				createMap("bence@gmail.com", "password"), VisitorResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getHeaders().getFirst("Set-Cookie").contains("JSESSIONID="));

		VisitorResource visitorResource = response.getBody();

		assertNotNull(visitorResource);
		assertEquals(3, visitorResource.getLinks().size());
		assertNotNull(visitorResource.getId().getHref());
		assertNotNull(visitorResource.getLink(AMUSEMENT_PARK));
		assertNotNull(visitorResource.getLink(UPLOAD_MONEY));

		assertEquals("bence@gmail.com", visitorResource.getEmail());
		assertEquals("ROLE_ADMIN", visitorResource.getAuthority());

		return visitorResource;
	}

	private MultiValueMap<String, String> createMap(String username, String password) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("email", username);
		map.add("password", password);
		return map;
	}

	private AmusementParkResource postAmusementPark() {
		AmusementParkResource amusementParkResource = ValidResourceFactory.createAmusementPark();

		ResponseEntity<AmusementParkResource> response = restTemplate.postForEntity(links.get(AMUSEMENT_PARK),
				amusementParkResource, AmusementParkResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AmusementParkResource responseAmusementParkResource = response.getBody();

		assertNotNull(responseAmusementParkResource);
		assertEquals(4, responseAmusementParkResource.getLinks().size());
		assertTrue(responseAmusementParkResource.getId().getHref()
				.endsWith(responseAmusementParkResource.getIdentifier().toString()));
		assertNotNull(responseAmusementParkResource.getLink(MACHINE));
		assertNotNull(responseAmusementParkResource.getLink(SIGN_UP));
		assertNotNull(responseAmusementParkResource.getLink(VISITOR_ENTER_PARK));

		amusementParkResource.setIdentifier(responseAmusementParkResource.getIdentifier());
		amusementParkResource.add(responseAmusementParkResource.getLinks());
		assertEquals(amusementParkResource, responseAmusementParkResource);

		return responseAmusementParkResource;
	}

	private String encode(String input) {
		try {
			return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new AmusementParkException("Wrong input!", e);
		}
	}

	@Test
	public void negativeTest() {
		loginAsAdmin();

		AmusementParkResource invalidAmusementParkResource = ValidResourceFactory.createAmusementPark();
		invalidAmusementParkResource.setEntranceFee(0);

		assertThrows(
				() -> restTemplate.postForObject(links.get(AMUSEMENT_PARK), invalidAmusementParkResource, Void.class),
				HttpClientErrorException.class, teaPotStatusAndEntranceFeeInvalidMessage());

		AmusementParkResource amusementParkResource = invalidAmusementParkResource;
		amusementParkResource.setEntranceFee(50);
		amusementParkResource.setCapital(500);
		ResponseEntity<AmusementParkResource> response = restTemplate.postForEntity(links.get(AMUSEMENT_PARK),
				amusementParkResource, AmusementParkResource.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		amusementParkResource = response.getBody();
		String machineLinkHref = amusementParkResource.getLink(MACHINE).getHref();

		MachineResource machineResource = ValidResourceFactory.createMachine();
		machineResource.setType("asd");

		assertThrows(() -> restTemplate.postForObject(machineLinkHref, machineResource, Void.class),
				HttpClientErrorException.class, teaPotStatusAndMachineTypeMustMatch());

		machineResource.setType(MachineType.CAROUSEL.toString());
		machineResource.setPrice(2000);

		assertThrows(() -> restTemplate.postForObject(machineLinkHref, machineResource, Void.class),
				HttpClientErrorException.class, teaPotStatusAndMachineTooExpensiveMessage());
	}

	@Test
	public void positiveTest() {
		VisitorResource visitorResource = loginAsAdmin();

		AmusementParkResource amusementParkResource = postAmusementPark();

		MachineResource machineResource = addMachine(amusementParkResource.getLink(MACHINE).getHref());

		visitorResource = enterPark(amusementParkResource.getLink(VISITOR_ENTER_PARK).getHref());

		visitorResource = getOnMachine(machineResource.getLink(GET_ON_MACHINE).getHref());

		visitorResource = getOffMachine(visitorResource.getLink(GET_OFF_MACHINE).getHref());

		addRegistry(visitorResource.getLink(ADD_REGISTRY).getHref());

		leavePark(visitorResource.getLink(VISITOR_LEAVE_PARK).getHref());

		deletePark(amusementParkResource.getId().getHref());
	}

	private MachineResource addMachine(String url) {
		ResponseEntity<MachineResource> response = restTemplate.postForEntity(url, ValidResourceFactory.createMachine(),
				MachineResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		MachineResource machineResource = response.getBody();

		assertNotNull(machineResource);
		assertEquals(2, machineResource.getLinks().size());
		assertTrue(machineResource.getId().getHref().endsWith(machineResource.getIdentifier().toString()));
		assertNotNull(machineResource.getLink(GET_ON_MACHINE));

		return machineResource;
	}

	private VisitorResource enterPark(String enterParkUrl) {
		ResponseEntity<VisitorResource> response = restTemplate.exchange(enterParkUrl, HttpMethod.PUT, HttpEntity.EMPTY,
				VisitorResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		VisitorResource visitorResource = response.getBody();

		assert6LinkInParkVisitor(visitorResource);

		return visitorResource;
	}

	private void assert6LinkInParkVisitor(VisitorResource visitorResource) {
		assertNotNull(visitorResource);
		assertEquals(6, visitorResource.getLinks().size());
		assertTrue(visitorResource.getId().getHref().endsWith(ME));
		assertNotNull(visitorResource.getLink(VISITOR_LEAVE_PARK));
		assertNotNull(visitorResource.getLink(GET_ON_MACHINE));
		assertNotNull(visitorResource.getLink(ADD_REGISTRY));
		assertNotNull(visitorResource.getLink(MACHINE));
		assertNotNull(visitorResource.getLink(UPLOAD_MONEY));
	}

	private VisitorResource getOnMachine(String getOnMachineUrl) {
		ResponseEntity<VisitorResource> response = restTemplate.exchange(getOnMachineUrl, HttpMethod.PUT,
				HttpEntity.EMPTY, VisitorResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		VisitorResource visitorResource = response.getBody();

		assertNotNull(visitorResource);
		assertEquals(3, visitorResource.getLinks().size());
		assertTrue(visitorResource.getId().getHref().endsWith(ME));
		assertNotNull(visitorResource.getLink(GET_OFF_MACHINE));
		assertNotNull(visitorResource.getLink(UPLOAD_MONEY));

		return visitorResource;
	}

	private VisitorResource getOffMachine(String getOffMachineUrl) {
		ResponseEntity<VisitorResource> response = restTemplate.exchange(getOffMachineUrl, HttpMethod.PUT,
				HttpEntity.EMPTY, VisitorResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		VisitorResource visitorResource = response.getBody();

		assert6LinkInParkVisitor(visitorResource);

		return visitorResource;
	}

	private void addRegistry(String addRegistryUrl) {
		ResponseEntity<GuestBookRegistryResource> response = restTemplate.postForEntity(addRegistryUrl,
				OPINION_ON_THE_PARK, GuestBookRegistryResource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		GuestBookRegistryResource guestBookRegistryResource = response.getBody();

		assertNotNull(guestBookRegistryResource);
		assertEquals(2, guestBookRegistryResource.getLinks().size());
		assertNotNull(guestBookRegistryResource.getId().getHref());
		assertNotNull(guestBookRegistryResource.getLink(ADD_REGISTRY));
	}

	private void leavePark(String leaveParkUrl) {
		restTemplate.put(leaveParkUrl, null);
	}

	private void deletePark(String amusementParkUrlWithId) {
		restTemplate.delete(amusementParkUrlWithId);
	}

	private ExceptionAsserter<HttpClientErrorException> teaPotStatusAndEntranceFeeInvalidMessage() {
		return exception -> {
			assertEquals(HttpStatus.I_AM_A_TEAPOT, exception.getStatusCode());
			assertEquals(validationError("entranceFee", rangeMessage(5, 200)), exception.getResponseBodyAsString());
		};
	}

	private ExceptionAsserter<HttpClientErrorException> teaPotStatusAndMachineTypeMustMatch() {
		return exception -> {
			assertEquals(HttpStatus.I_AM_A_TEAPOT, exception.getStatusCode());
			assertEquals(
					validationError("type", oneOfMessage(
							Stream.of(MachineType.values()).map(MachineType::toString).collect(toSet()).toString())),
					exception.getResponseBodyAsString());
		};
	}

	private ExceptionAsserter<HttpClientErrorException> teaPotStatusAndMachineTooExpensiveMessage() {
		return exception -> {
			assertEquals(HttpStatus.I_AM_A_TEAPOT, exception.getStatusCode());
			assertEquals(MACHINE_IS_TOO_EXPENSIVE, exception.getResponseBodyAsString());
		};
	}
}
