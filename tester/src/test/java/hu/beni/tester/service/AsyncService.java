package hu.beni.tester.service;

import static hu.beni.clientsupport.Client.uri;
import static hu.beni.clientsupport.ResponseType.AMUSEMENT_PARK_TYPE;
import static hu.beni.clientsupport.ResponseType.MACHINE_TYPE;
import static hu.beni.clientsupport.ResponseType.RESOURCES_MACHINE_TYPE;
import static hu.beni.clientsupport.ResponseType.VISITOR_TYPE;
import static hu.beni.clientsupport.constants.HATEOASLinkRelConstants.ADD_REGISTRY;
import static hu.beni.clientsupport.constants.HATEOASLinkRelConstants.GET_OFF_MACHINE;
import static hu.beni.clientsupport.constants.HATEOASLinkRelConstants.GET_ON_MACHINE;
import static hu.beni.clientsupport.constants.HATEOASLinkRelConstants.MACHINE;
import static hu.beni.clientsupport.constants.HATEOASLinkRelConstants.VISITOR_ENTER_PARK;
import static hu.beni.clientsupport.constants.HATEOASLinkRelConstants.VISITOR_LEAVE_PARK;
import static hu.beni.tester.constant.Constants.AMUSEMENT_PARK_URL;
import static hu.beni.tester.constant.Constants.GUEST_BOOK_REGISTRY_TEXT;
import static hu.beni.tester.constant.Constants.LOGIN_URL;
import static hu.beni.tester.constant.Constants.LOGOUT_URL;
import static hu.beni.tester.constant.Constants.PASS;
import static hu.beni.tester.constant.Constants.PASSWORD;
import static hu.beni.tester.constant.Constants.USERNAME;
import static hu.beni.tester.constant.Constants.VISITOR_URL;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.context.annotation.Scope;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.TypeReferences.PagedResourcesType;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import hu.beni.clientsupport.Client;
import hu.beni.clientsupport.ResponseType;
import hu.beni.clientsupport.resource.AmusementParkResource;
import hu.beni.clientsupport.resource.VisitorResource;
import hu.beni.tester.dto.DeleteTime;
import hu.beni.tester.dto.SumAndTime;
import hu.beni.tester.dto.VisitorStuffTime;
import hu.beni.tester.factory.ResourceFactory;
import hu.beni.tester.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;

@Async
@Service
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class AsyncService {

	public static final PagedResourcesType<ResourceSupport> PAGED_TYPE = new PagedResourcesType<ResourceSupport>() {
	};

	private final Client client;
	private final String username;
	private final ResourceFactory resourceFactory;
	private final ApplicationProperties properties;

	public CompletableFuture<Void> login() {
		client.post(uri(LOGIN_URL), MediaType.APPLICATION_FORM_URLENCODED, createMapWithUsernameAndPass(), Void.class);
		return CompletableFuture.completedFuture(null);
	}

	private MultiValueMap<String, String> createMapWithUsernameAndPass() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(USERNAME, username);
		map.add(PASSWORD, PASS);
		return map;
	}

	public CompletableFuture<?> logout() {
		client.post(uri(LOGOUT_URL), null, Void.class);
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<DeleteTime> deleteAllPark() {
		List<Long> tenParkTimes = new LinkedList<>();
		long start = now();
		deleteAllOnUrl(AMUSEMENT_PARK_URL, tenParkTimes);
		return CompletableFuture.completedFuture(new DeleteTime(millisFrom(start), tenParkTimes));
	}

	private void deleteAllOnUrl(String url, List<Long> tenTimes) {
		boolean thereIsStillData;
		do {
			long tenStart = now();
			thereIsStillData = getPageDeleteAllFalseIfNoMore(url);
			if (thereIsStillData) {
				tenTimes.add(millisFrom(tenStart));
			}
		} while (thereIsStillData);
	}

	private boolean getPageDeleteAllFalseIfNoMore(String url) {
		Collection<ResourceSupport> data = client.get(uri(url), PAGED_TYPE).getBody().getContent();
		data.stream().map(ResourceSupport::getId).map(Link::getHref).forEach(href -> client.delete(uri(href)));
		return !data.isEmpty();
	}

	public CompletableFuture<Long> createAmusementParksWithMachines() {
		long start = now();
		createAmusementParks().map(this::mapToMachineLinkHref).forEach(this::createMachines);
		return CompletableFuture.completedFuture(millisFrom(start));
	}

	private Stream<AmusementParkResource> createAmusementParks() {
		return IntStream.range(0, properties.getNumberOf().getAmusementParksPerAdmin())
				.mapToObj(i -> createAmusementPark());
	}

	private AmusementParkResource createAmusementPark() {
		return client
				.post(uri(AMUSEMENT_PARK_URL), resourceFactory.createAmusementParkWithAddress(), AMUSEMENT_PARK_TYPE)
				.getBody();
	}

	private String mapToMachineLinkHref(AmusementParkResource amusementParkResource) {
		return amusementParkResource.getLink(MACHINE).getHref();
	}

	private void createMachines(String machineUrl) {
		IntStream.range(0, properties.getNumberOf().getMachinesPerPark()).forEach(i -> addMachine(machineUrl));
	}

	private void addMachine(String machineUrl) {
		client.post(uri(machineUrl), resourceFactory.createMachine(), MACHINE_TYPE);
	}

	public CompletableFuture<SumAndTime> sumAmusementParksCapital() {
		long start = now();
		long sum = sum(AMUSEMENT_PARK_URL, AmusementParkResource.class, AmusementParkResource::getCapital);
		return CompletableFuture.completedFuture(new SumAndTime(sum, millisFrom(start)));
	}

	private <T> long sum(String url, Class<T> clazz, ToIntFunction<T> toIntFunction) {
		Optional<String> nextPageUrl = Optional.of(url);
		long sum = 0;
		do {
			PagedResources<T> page = client.get(uri(nextPageUrl.get()), ResponseType.getPagedType(clazz)).getBody();
			nextPageUrl = Optional.ofNullable(page.getNextLink()).map(Link::getHref);
			sum += page.getContent().stream().mapToInt(toIntFunction).sum();
		} while (nextPageUrl.isPresent());

		return sum;
	}

	public CompletableFuture<VisitorStuffTime> visitAllStuffInEveryPark() {
		List<Long> oneParkTimes = new LinkedList<>();
		List<Long> tenParkTimes = new LinkedList<>();
		long start = now();
		visitAllStuffInEveryPark(oneParkTimes, tenParkTimes);
		return CompletableFuture.completedFuture(new VisitorStuffTime(millisFrom(start), tenParkTimes, oneParkTimes));
	}

	private void visitAllStuffInEveryPark(List<Long> oneParkTimes, List<Long> tenParkTimes) {
		Optional<String> nextPageUrl = Optional.of(AMUSEMENT_PARK_URL);
		VisitorResource visitorResource = client.post(uri(VISITOR_URL), resourceFactory.createVisitor(), VISITOR_TYPE)
				.getBody();
		do {
			long tenParkStart = now();
			PagedResources<AmusementParkResource> page = client
					.get(uri(nextPageUrl.get()), ResponseType.getPagedType(AmusementParkResource.class)).getBody();
			nextPageUrl = Optional.ofNullable(page.getNextLink()).map(Link::getHref);
			visitEverythingInParks(page.getContent(), visitorResource.getIdentifier(), oneParkTimes);
			tenParkTimes.add(millisFrom(tenParkStart));
		} while (nextPageUrl.isPresent());
	}

	private void visitEverythingInParks(Collection<AmusementParkResource> amusementParkResources, Long visitorId,
			List<Long> oneParkTimes) {
		amusementParkResources.stream().map(this::mapToEnterParkUrl)
				.forEach(enterParkUrl -> visitEverythingInAPark(uri(enterParkUrl, visitorId), oneParkTimes));
	}

	private String mapToEnterParkUrl(AmusementParkResource amusementParkResource) {
		return amusementParkResource.getLink(VISITOR_ENTER_PARK).getHref();
	}

	private void visitEverythingInAPark(URI enterParkUrl, List<Long> oneParkTimes) {
		long startPark = now();
		VisitorResource visitorResource = client.put(enterParkUrl, null, VISITOR_TYPE).getBody();
		getMachinesAndGetOnAndOff(visitorResource);
		addRegistryAndLeave(visitorResource);
		oneParkTimes.add(millisFrom(startPark));
	}

	private void getMachinesAndGetOnAndOff(VisitorResource visitorResource) {
		client.get(uri(visitorResource.getLink(MACHINE).getHref()), RESOURCES_MACHINE_TYPE).getBody().getContent()
				.stream()
				.forEach(machineResource -> getOnAndOffMachine(machineResource.getLink(GET_ON_MACHINE).getHref(),
						visitorResource.getIdentifier()));
	}

	private void getOnAndOffMachine(String getOnMachineUrl, Long visitorId) {
		VisitorResource onMachineVisitor = client.put(uri(getOnMachineUrl, visitorId), null, VISITOR_TYPE).getBody();
		client.put(uri(onMachineVisitor.getLink(GET_OFF_MACHINE).getHref()), null, Void.class);
	}

	private void addRegistryAndLeave(VisitorResource visitorResource) {
		client.post(uri(visitorResource.getLink(ADD_REGISTRY).getHref()), GUEST_BOOK_REGISTRY_TEXT, Void.class);
		client.put(uri(visitorResource.getLink(VISITOR_LEAVE_PARK).getHref()), null, Void.class);
	}

	public CompletableFuture<SumAndTime> sumVisitorsSpendingMoney() {
		long start = now();
		long sum = sum(VISITOR_URL, VisitorResource.class, VisitorResource::getSpendingMoney);
		return CompletableFuture.completedFuture(new SumAndTime(sum, millisFrom(start)));
	}

	public CompletableFuture<DeleteTime> deleteAllVisitor() {
		List<Long> tenVisitorTimes = new LinkedList<>();
		long start = now();
		deleteAllOnUrl(VISITOR_URL, tenVisitorTimes);
		return CompletableFuture.completedFuture(new DeleteTime(millisFrom(start), tenVisitorTimes));
	}

	private long now() {
		return System.currentTimeMillis();
	}

	private long millisFrom(long start) {
		return now() - start;
	}
}
