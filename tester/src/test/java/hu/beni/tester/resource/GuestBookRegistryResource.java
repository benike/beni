package hu.beni.tester.resource;

import java.time.LocalDateTime;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@Data
public class GuestBookRegistryResource extends ResourceSupport {

	private Long identifier;

	private String textOfRegistry;

	private LocalDateTime dateOfRegistry;

	private Long visitorId;

}