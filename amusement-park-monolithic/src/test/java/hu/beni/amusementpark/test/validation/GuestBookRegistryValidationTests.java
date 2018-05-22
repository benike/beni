package hu.beni.amusementpark.test.validation;

import static hu.beni.amusementpark.constants.FieldNameConstants.AMUSEMENT_PARK;
import static hu.beni.amusementpark.constants.FieldNameConstants.TEXT_OF_REGISTRY;
import static hu.beni.amusementpark.constants.FieldNameConstants.VISITOR;
import static hu.beni.amusementpark.constants.StringParamConstants.OPINION_ON_THE_PARK;
import static hu.beni.amusementpark.constants.StringParamConstants.STRING_WITH_101_LENGTH;
import static hu.beni.amusementpark.constants.StringParamConstants.STRING_WITH_4_LENGTH;
import static hu.beni.amusementpark.constants.ValidationMessageConstants.NOT_NULL_MESSAGE;
import static hu.beni.amusementpark.constants.ValidationMessageConstants.SIZE_5_100_MESSAGE;

import org.junit.Before;
import org.junit.Test;

import hu.beni.amusementpark.entity.AmusementPark;
import hu.beni.amusementpark.entity.GuestBookRegistry;
import hu.beni.amusementpark.entity.Visitor;

public class GuestBookRegistryValidationTests extends AbstractValidation<GuestBookRegistry> {

	private GuestBookRegistry guestBookRegistry;

	@Before
	public void setUp() {
		guestBookRegistry = GuestBookRegistry.builder().textOfRegistry(OPINION_ON_THE_PARK)
				.visitor(Visitor.builder().id(1000L).build()).amusementPark(AmusementPark.builder().id(1001L).build())
				.build();
	}

	@Test
	public void validAmusementPark() {
		validateAndAssertNoViolations(guestBookRegistry);
	}

	@Test
	public void invalidTextOfRegistry() {
		guestBookRegistry.setTextOfRegistry(null);
		validateAndAssertViolationsSizeIsOne(guestBookRegistry);
		assertInvalidValueAndPropertyNameAndMessageEquals(guestBookRegistry.getTextOfRegistry(), TEXT_OF_REGISTRY,
				NOT_NULL_MESSAGE);

		guestBookRegistry.setTextOfRegistry(STRING_WITH_4_LENGTH);
		validateAndAssertViolationsSizeIsOne(guestBookRegistry);
		assertInvalidValueAndPropertyNameAndMessageEquals(guestBookRegistry.getTextOfRegistry(), TEXT_OF_REGISTRY,
				SIZE_5_100_MESSAGE);

		guestBookRegistry.setTextOfRegistry(STRING_WITH_101_LENGTH);
		validateAndAssertViolationsSizeIsOne(guestBookRegistry);
		assertInvalidValueAndPropertyNameAndMessageEquals(guestBookRegistry.getTextOfRegistry(), TEXT_OF_REGISTRY,
				SIZE_5_100_MESSAGE);
	}

	@Test
	public void nullAmusementPark() {
		guestBookRegistry.setAmusementPark(null);
		validateAndAssertViolationsSizeIsOne(guestBookRegistry);
		assertInvalidValueAndPropertyNameAndMessageEquals(guestBookRegistry.getAmusementPark(), AMUSEMENT_PARK,
				NOT_NULL_MESSAGE);
	}

	@Test
	public void nullVisitor() {
		guestBookRegistry.setVisitor(null);
		validateAndAssertViolationsSizeIsOne(guestBookRegistry);
		assertInvalidValueAndPropertyNameAndMessageEquals(guestBookRegistry.getVisitor(), VISITOR, NOT_NULL_MESSAGE);
	}
}
