package hu.beni.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmusementParkDTO {

	private Long identifier;

	private String name;

	private Integer capital;

	private Integer totalArea;

	private Integer entranceFee;

	private AddressDTO address;

}
